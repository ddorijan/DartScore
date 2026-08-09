# Firebase setup (production mode)

## 1) Firebase Console
- Use your current Firebase project (production mode).
- In Build -> Authentication -> Sign-in method, enable Email/Password.
- In Build -> Firestore Database, create database in production mode.
- In Firestore Rules tab, paste content from firestore.rules and publish.

## 2) What app now does automatically
- Register screen creates Firebase Auth user.
- After successful register, app creates users/{uid} in Firestore.
- Login screen signs user in.

## 3) users/{uid} fields created by app
- displayName: string
- email: string
- birthDate: string (optional)
- avatarUrl: string
- country: string
- createdAt: server timestamp
- defaultGameSettings: map
  - startScore: number (default 501)
  - doubleIn: boolean (default false)
  - doubleOut: boolean (default true)
  - setsOrLegs: string (default "legs")

## 4) Next manual collections (only when we implement feature)
- lobbies
- feedPosts
- matches
- friends/{uid}/list
- trainingSessions
- statsDaily

You do not need to create these collections now. Firestore creates them automatically when first document is written.
