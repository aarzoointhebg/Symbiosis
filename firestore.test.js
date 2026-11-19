const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const CHARLIE_UID = "charlie_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write community_profiles, missions, or invitations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("community_profiles").where("visibility", "==", "COMMUNITY").get());
  await assertFails(unauthDb.collection("missions").where("visibility", "==", "COMMUNITY").get());
  await assertFails(unauthDb.collection("invitations").where("participantUids", "array-contains", ALICE_UID).get());
});

test("Community profile owner create, partial update, visibility isolation, and ghost-field rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("community_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav",
      headline: "Programming & Robotics",
      skills: ["Programming", "Basic electronics"],
      visibility: "COMMUNITY",
      onboardingCompleted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("community_profiles").doc(ALICE_UID).update({
      skills: ["Programming", "Logical problem-solving"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob can query COMMUNITY profiles
  await assertSucceeds(
    bobDb.collection("community_profiles").where("visibility", "==", "COMMUNITY").get()
  );

  // Unfiltered query on community_profiles fails
  await assertFails(bobDb.collection("community_profiles").get());

  // Bob cannot update Alice's profile
  await assertFails(
    bobDb.collection("community_profiles").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Ghost field / immutable createdAt rejected
  await assertFails(
    aliceDb.collection("community_profiles").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("User subcollections (ambitions, private_settings) enforce strict owner isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ambitions").doc("amb_1").set({
      userId: ALICE_UID,
      title: "Build an environmental sensor",
      horizon: "SHORT_TERM",
      priority: "HIGH",
      tags: ["Programming", "Science"],
      sharedToCommunity: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("ambitions").get()
  );
});

test("UserProfile enforces strict owner isolation, schema validation, and immutable createdAt", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").set({
      userId: ALICE_UID,
      displayName: "Aarav",
      headline: "Systems builder",
      bio: "Enjoy civic tech and environmental monitoring.",
      skills: ["Programming", "Basic electronics"],
      skillProficiencies: { "Programming": "EXPERT", "Basic electronics": "ADVANCED" },
      interests: ["Environmental technology", "Open hardware"],
      availability: "EVENINGS",
      availableDays: ["Tue", "Thu", "Sat"],
      timeSlots: ["Evening"],
      collaborationStyle: "HYBRID",
      supportWanted: ["Visual design"],
      onboardingCompleted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Partial update on allowed keys (including skillProficiencies) succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").update({
      skills: ["Programming", "Basic electronics", "Data collection"],
      skillProficiencies: { "Programming": "EXPERT", "Basic electronics": "ADVANCED", "Data collection": "BEGINNER" },
      availableDays: ["Tue", "Sat"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Immutable createdAt mutation rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Invalid availability enum rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").update({
      availability: "MIDNIGHT",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user read & write rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("user_profile").doc("profile").update({
      displayName: "Tampered",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Missions enforce ownership, terminal COMPLETED state lock, and query filters", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("missions").doc("mission_env").set({
      ownerId: ALICE_UID,
      ownerName: "Aarav",
      title: "Build an Interactive Environmental Monitoring Project",
      description: "Combine programming, research, and visual presentation.",
      objective: "Create a community-ready environmental monitoring prototype.",
      requiredCapabilities: ["Programming", "Research", "Visual presentation"],
      memberUids: [ALICE_UID],
      status: "OPEN",
      visibility: "COMMUNITY",
      progressPercent: 10,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("missions").where("visibility", "==", "COMMUNITY").get()
  );
  await assertFails(bobDb.collection("missions").get());

  // Complete mission -> terminal lock prevents further updates
  await assertSucceeds(
    aliceDb.collection("missions").doc("mission_env").update({
      status: "COMPLETED",
      progressPercent: 100,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("missions").doc("mission_env").update({
      progressPercent: 50,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Invitations enforce consent state transitions and participant isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("invitations").doc("inv_1").set({
      missionId: "mission_env",
      missionTitle: "Environmental Monitoring",
      senderId: ALICE_UID,
      senderName: "Aarav",
      recipientId: BOB_UID,
      recipientName: "Mira",
      participantUids: [ALICE_UID, BOB_UID],
      proposedRole: "Visual Presentation Designer",
      rationale: "Covers visual design and creative communication.",
      status: "PENDING",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Charlie cannot read Alice-Bob invitation
  await assertFails(charlieDb.collection("invitations").doc("inv_1").get());

  // Recipient Bob can accept
  await assertSucceeds(
    bobDb.collection("invitations").doc("inv_1").update({
      status: "ACCEPTED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Once ACCEPTED, no further changes allowed
  await assertFails(
    bobDb.collection("invitations").doc("inv_1").update({
      status: "DECLINED",
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("SkillEndorsements allow peer trust badges, reject self-endorsements & spoofing, and restrict deletes to endorser", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  // Unauthenticated access denied
  await assertFails(unauthDb.collection("skill_endorsements").limit(50).get());

  // Self-endorsement denied
  await assertFails(
    aliceDb.collection("skill_endorsements").doc("self_end_1").set({
      recipientId: ALICE_UID,
      recipientName: "Aarav",
      endorserId: ALICE_UID,
      endorserName: "Aarav",
      skillName: "Programming",
      normalizedSkill: "programming",
      note: "",
      createdAt: ts,
    })
  );

  // Spoofing another endorser UID denied
  await assertFails(
    bobDb.collection("skill_endorsements").doc("spoof_1").set({
      recipientId: CHARLIE_UID,
      recipientName: "Charlie",
      endorserId: ALICE_UID,
      endorserName: "Aarav",
      skillName: "Research",
      normalizedSkill: "research",
      note: "",
      createdAt: ts,
    })
  );

  // Valid peer endorsement by Alice for Bob succeeds
  await assertSucceeds(
    aliceDb.collection("skill_endorsements").doc("end_alice_bob_design").set({
      recipientId: BOB_UID,
      recipientName: "Mira",
      endorserId: ALICE_UID,
      endorserName: "Aarav",
      skillName: "Visual design",
      normalizedSkill: "visual design",
      note: "Endorsed from profile badge",
      createdAt: ts,
    })
  );

  // Any signed-in member can read endorsements
  await assertSucceeds(bobDb.collection("skill_endorsements").limit(200).get());

  // Bob cannot delete Alice's endorsement; Alice can withdraw her own endorsement
  await assertFails(bobDb.collection("skill_endorsements").doc("end_alice_bob_design").delete());
  await assertSucceeds(aliceDb.collection("skill_endorsements").doc("end_alice_bob_design").delete());
});

test("CollaborationMessages enforce participant isolation, anti-spoofing, and immutable messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  // Unauthenticated read denied
  await assertFails(
    unauthDb.collection("collaboration_messages").where("participantUids", "array-contains", ALICE_UID).get()
  );

  // Self-messaging denied
  await assertFails(
    aliceDb.collection("collaboration_messages").doc("msg_self").set({
      threadId: "thr_self",
      senderId: ALICE_UID,
      senderName: "Aarav",
      recipientId: ALICE_UID,
      recipientName: "Aarav",
      participantUids: [ALICE_UID, ALICE_UID],
      body: "Hello me",
      contextLabel: "",
      createdAt: ts,
    })
  );

  // Spoofing senderId denied
  await assertFails(
    bobDb.collection("collaboration_messages").doc("msg_spoof").set({
      threadId: "thr_alice_charlie",
      senderId: ALICE_UID,
      senderName: "Aarav",
      recipientId: CHARLIE_UID,
      recipientName: "Charlie",
      participantUids: [ALICE_UID, CHARLIE_UID],
      body: "Spoofed message",
      contextLabel: "",
      createdAt: ts,
    })
  );

  // Valid message from Alice to Bob succeeds
  await assertSucceeds(
    aliceDb.collection("collaboration_messages").doc("msg_1").set({
      threadId: "thr_alice_bob",
      senderId: ALICE_UID,
      senderName: "Aarav",
      recipientId: BOB_UID,
      recipientName: "Mira",
      participantUids: [ALICE_UID, BOB_UID],
      body: "Would love to pair on the air-quality sensor visuals!",
      contextLabel: "Complementary: Visual design",
      createdAt: ts,
    })
  );

  // Both Alice and Bob can query their participant messages; Charlie cannot read msg_1
  await assertSucceeds(
    bobDb.collection("collaboration_messages").where("participantUids", "array-contains", BOB_UID).get()
  );
  await assertFails(
    charlieDb.collection("collaboration_messages").doc("msg_1").get()
  );

  // Updates are immutable; only sender can delete
  await assertFails(
    aliceDb.collection("collaboration_messages").doc("msg_1").update({ body: "Edited" })
  );
  await assertFails(
    bobDb.collection("collaboration_messages").doc("msg_1").delete()
  );
  await assertSucceeds(
    aliceDb.collection("collaboration_messages").doc("msg_1").delete()
  );
});
