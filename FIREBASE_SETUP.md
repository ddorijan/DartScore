# Firebase setup (production mode)

## 1) Firebase Console
- Use your current Firebase project (production mode).
- In Build -> Authentication -> Sign-in method, enable Email/Password.
- In Build -> Firestore Database, create database in production mode.
- In Firestore Rules tab, paste content from firestore.rules and publish.

## 1b) Enable Google Sign-In
The app's "Nastavi s Googleom" button uses Credential Manager + Firebase Auth, but it
needs a Web Client ID before it can work:
1. Firebase Console -> Build -> Authentication -> Sign-in method -> enable **Google**.
2. After enabling, Firebase shows a "Web SDK configuration" -> **Web client ID**
   (this is also visible in Google Cloud Console -> APIs & Services -> Credentials,
   as the "Web client (auto created by Google Service)" OAuth client).
3. Copy that ID into [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml),
   replacing the `default_web_client_id` placeholder value.
4. Add your debug and release SHA-1 certificate fingerprints to the Android app in
   Firebase Console -> Project settings -> Your apps (`./gradlew signingReport` prints them),
   then re-download `google-services.json` and replace the one in `app/`.

Without steps 3-4 the Google button will show an error instead of signing the user in.

## 2) What app now does automatically
- Register screen creates Firebase Auth user.
- After successful register, app creates users/{uid} in Firestore.
- Login screen signs user in.
- Both screens also support "Nastavi s Googleom", which signs in via Credential Manager
  and creates the users/{uid} Firestore document on first sign-in if it doesn't exist yet.

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
