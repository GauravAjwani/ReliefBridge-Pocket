# GitHub Submission Checklist

Use this guide to upload ReliefBridge Pocket for the Arm AI Optimization
Challenge 2026.

## Before You Upload

Confirm these files exist in the repo:

| Required | Path |
| --- | --- |
| License (MIT) | `LICENSE` |
| Project README | `README.md` |
| Android app | `mobile/` |
| Devpost copy | `docs/devpost-description.md` |
| Optimization notes | `docs/arm-ai-optimization.md` |
| Demo video script | `docs/mobile-demo-script.md` |

Do **not** upload:

- `node_modules/`
- `mobile/build/` or `mobile/app/build/`
- `mobile/.gradle/`
- `mobile/.idea/`
- `.env` (only `.env.sample` is safe)
- `*.apk` files

The `.gitignore` file already excludes these.

## Step 1: Create a GitHub Repository

1. Go to [https://github.com/new](https://github.com/new)
2. Repository name example: `reliefbridge-pocket`
3. Set visibility to **Public**
4. Do **not** add a README, license, or `.gitignore` (this project already has them)
5. Click **Create repository**

## Step 2: Initialize Git and Push

Open PowerShell in the project folder:

```powershell
cd c:\Users\Admin\Projects\threadbridge

git init
git add .
git status
```

Review `git status`. You should **not** see `node_modules`, `build/`, or `.env`.

Then commit and push:

```powershell
git commit -m "ReliefBridge Pocket: Arm Mobile AI hackathon submission"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/reliefbridge-pocket.git
git push -u origin main
```

Replace `YOUR_USERNAME/reliefbridge-pocket` with your actual repo URL.

## Step 3: Set GitHub About Section

On your repo page, click the **gear icon** next to **About** and set:

- **Description:** On-device disaster relief triage for Arm Android with built-in AI optimization benchmarks
- **Website:** your Devpost submission URL (after you submit)
- **Topics:** `arm`, `android`, `mobile-ai`, `on-device-ai`, `hackathon`, `kotlin`
- **License:** MIT (should auto-detect from `LICENSE`)

Judges look for a visible open-source license in the About section.

## Step 4: Verify Judges Can Build It

Ask yourself:

1. Can someone clone the repo and open `mobile/` in Android Studio?
2. Does `README.md` explain how to run on an Arm Android device?
3. Is the MIT license visible at the top of the repo?

Test locally:

```powershell
cd mobile
.\gradlew.bat :app:assembleDebug
```

Or open `mobile/` in Android Studio and press **Run** on a connected phone.

## Step 5: Devpost Submission Fields

Copy from `docs/devpost-description.md`:

- **Project overview**
- **Functionality / output**
- **Setup instructions**
- **Repo URL:** `https://github.com/YOUR_USERNAME/reliefbridge-pocket`
- **Video URL:** your YouTube demo (under 3 minutes)
- **Proof artifacts:** screenshots of Benchmark tab + exported `benchmark-results.md`

Track: **Mobile AI**

## Step 6: Optional Proof Screenshots

Save these from your phone for Devpost:

1. Triage tab with action queue
2. Benchmark tab with speedup numbers
3. Export/share proof screen
4. Contents of `benchmark-results.md` after export

See `docs/sample-benchmark-results.md` for the expected report format.
