import {setGlobalOptions} from "firebase-functions";
import {onSchedule} from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";
import fetch from "node-fetch";

admin.initializeApp();
const db = admin.firestore();

setGlobalOptions({maxInstances: 10});

// Rating levels: 800 to 3500, step 300 (last step 400)
const RATINGS = [800, 1100, 1400, 1700, 2000, 2300, 2600, 2900, 3200, 3500];

interface CfProblem {
  contestId: number;
  index: string;
  name: string;
  rating?: number;
  tags: string[];
}

interface CfProblemsResponse {
  status: string;
  result: {
    problems: CfProblem[];
    problemStatistics: unknown[];
  };
}

interface CfSubmission {
  id: number;
  creationTimeSeconds: number;
  problem: CfProblem;
  author: {
    members: { handle: string }[];
  };
  verdict?: string;
}

interface CfContestStatusResponse {
  status: string;
  result: CfSubmission[];
}

/**
 * Returns today's date as YYYY-MM-DD.
 * @return {string} Date string.
 */
function getTodayDate(): string {
  const now = new Date();
  return now.toISOString().split("T")[0]; // YYYY-MM-DD
}

/**
 * Fetch all CF problems and pick one random problem per rating level.
 * Stores the selected problems in dailyProblems/{date}.
 */
export const selectDailyProblems = onSchedule("every day 00:00", async () => {
  const date = getTodayDate();

  // Check if already selected for today
  const existing = await db.doc(`dailyProblems/${date}`).get();
  if (existing.exists) {
    console.log(`Daily problems already selected for ${date}`);
    return;
  }

  // Fetch all CF problems
  const res = await fetch("https://codeforces.com/api/problemset.problems");
  const data = (await res.json()) as CfProblemsResponse;

  if (data.status !== "OK") {
    throw new Error("Failed to fetch CF problems");
  }

  const allProblems = data.result.problems.filter((p) => p.rating);

  // Group by rating
  const byRating = new Map<number, CfProblem[]>();
  for (const p of allProblems) {
    const r = p.rating as number;
    const list = byRating.get(r) || [];
    list.push(p);
    byRating.set(r, list);
  }

  // Pick one random problem per rating level
  const selected: Record<string, {
    contestId: number;
    index: string;
    name: string;
    rating: number;
  }> = {};

  for (const rating of RATINGS) {
    const candidates = byRating.get(rating);
    if (!candidates || candidates.length === 0) {
      console.warn(`No problems found for rating ${rating}`);
      continue;
    }
    const pick = candidates[Math.floor(Math.random() * candidates.length)];
    selected[String(rating)] = {
      contestId: pick.contestId,
      index: pick.index,
      name: pick.name,
      rating: rating,
    };
  }

  await db.doc(`dailyProblems/${date}`).set({
    date,
    problems: selected,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  console.log(`Selected daily problems for ${date}:`, selected);
});

/**
 * Check submissions for today's daily problems every 15 minutes.
 * Uses contest.status API per problem (9 calls).
 */
export const checkSubmissions = onSchedule("every 15 minutes", async () => {
  const date = getTodayDate();

  // Get today's problems
  const problemsDoc = await db.doc(`dailyProblems/${date}`).get();
  if (!problemsDoc.exists) {
    console.log(`No daily problems for ${date}`);
    return;
  }

  const {problems} = problemsDoc.data() as {
    problems: Record<string, {
      contestId: number;
      index: string;
      name: string;
      rating: number;
    }>;
  };

  // Get registered users
  const usersSnapshot = await db.collection("users").get();
  const registeredHandles = new Set<string>(
    usersSnapshot.docs.map((doc) => doc.id.toLowerCase())
  );

  if (registeredHandles.size === 0) {
    console.log("No registered users");
    return;
  }

  // Get or create today's leaderboard doc
  const leaderboardRef = db.doc(`dailyLeaderboard/${date}`);
  const leaderboardDoc = await leaderboardRef.get();
  const leaderboardData = leaderboardDoc.exists ?
    (leaderboardDoc.data() as Record<string, unknown>) :
    {scores: {}, submissions: []};

  const existingScores = leaderboardData.scores as Record<string, number>;
  const existingSubmissions = leaderboardData.submissions as Array<{
    handle: string;
    contestId: number;
    index: string;
    rating: number;
    timestamp: number;
  }>;

  // Track already-credited submissions to avoid duplicates
  const credited = new Set<string>(
    existingSubmissions.map(
      (s) => `${s.handle.toLowerCase()}:${s.contestId}${s.index}`
    )
  );

  // Start of today (UTC)
  const todayStart = new Date(date + "T00:00:00Z").getTime() / 1000;

  // Check each problem via contest.status
  for (const [, problem] of Object.entries(problems)) {
    try {
      const base = "https://codeforces.com/api";
      const url = `${base}/contest.status` +
        `?contestId=${problem.contestId}` +
        "&from=1&count=5000";
      const res = await fetch(url);
      const data = (await res.json()) as CfContestStatusResponse;

      if (data.status !== "OK") {
        console.warn(`Failed to fetch contest ${problem.contestId} status`);
        continue;
      }

      for (const sub of data.result) {
        // Only accepted submissions for the right problem, from today
        if (sub.verdict !== "OK") continue;
        if (sub.problem.index !== problem.index) continue;
        if (sub.creationTimeSeconds < todayStart) continue;

        const handle = sub.author.members[0]?.handle;
        if (!handle) continue;

        const handleLower = handle.toLowerCase();
        if (!registeredHandles.has(handleLower)) continue;

        const key = `${handleLower}:${problem.contestId}${problem.index}`;
        if (credited.has(key)) continue;

        // Credit this submission
        credited.add(key);
        existingScores[handle] = (existingScores[handle] || 0) + problem.rating;
        existingSubmissions.push({
          handle,
          contestId: problem.contestId,
          index: problem.index,
          rating: problem.rating,
          timestamp: sub.creationTimeSeconds,
        });
      }

      // Rate limit: small delay between API calls
      await new Promise((resolve) => setTimeout(resolve, 2000));
    } catch (err) {
      const pid = `${problem.contestId}${problem.index}`;
      console.error(`Error checking problem ${pid}:`, err);
    }
  }

  await leaderboardRef.set({
    date,
    scores: existingScores,
    submissions: existingSubmissions,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  console.log(`Updated leaderboard for ${date}`);
});
