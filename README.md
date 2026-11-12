# Loksewa Study Book (लोक सेवा आयोग अध्ययन पुस्तक)

A modern, native Android application built with Kotlin and Jetpack Compose for candidates preparing for Nepal's Public Service Commission (Loksewa Aayog — प्रदेश लोक सेवा आयोग) 5th Level (Non-Technical) Paper I examination.

## Features

- **Complete Study Book (15 Parts)**:
  - Front matter: About the book, Master Index, Seven-Province Syllabus Comparison & Priority Classification, Study Strategy, and Chapter Roadmap.
  - 10 Complete Chapters:
    1. Geography of Nepal (नेपालको भूगोल)
    2. History of Nepal (नेपालको इतिहास)
    3. Constitution of Nepal (नेपालको संविधान)
    4. Federalism and Governance (संघीयता र शासन प्रणाली)
    5. Economy and Public Finance (अर्थतन्त्र र सार्वजनिक वित्त)
    6. Society, Culture and Heritage (समाज, संस्कृति र सम्पदा)
    7. Science, Technology and Environment (विज्ञान, प्रविधि र वातावरण)
    8. International Relations and Organisations (अन्तर्राष्ट्रिय सम्बन्ध र सङ्गठन)
    9. Current Affairs (समसामयिक गतिविधि)
    10. Koshi Province (कोशी प्रदेश)
- **Practice MCQs Engine**:
  - 710 practice multiple-choice questions organized by chapter and syllabus sub-topics.
  - Instant visual feedback on every question with correct/incorrect status, official tags (`[Expected – Practice]`, `[Pattern-Based – Reconstructed]`), and thorough explanations.
  - "Questions I Got Wrong" review mode to drill down on missed questions.
- **Official Timed Mock Tests**:
  - Configurable tests with 10, 25, or 50 questions.
  - Official timing: 54 seconds per question (50 questions in 45 minutes) or untimed.
  - Official Loksewa marking arithmetic: **+2.0 marks** for each correct answer, **−0.4 marks deducted** for each wrong answer (20% penalty), and **0 marks** for questions left blank.
  - Active countdown timer, question palette grid, and blank warning dialogs.
  - Full post-test review with score breakdown and detailed question-by-question explanations.
  - Local test history log keeping track of past mock attempts.
- **Instant Full-Text Search**:
  - Fast search across all 1,191 indexed sections in the book.
  - Contextual keyword highlighting and direct jumping to chapter sections.
- **Customizable Reader Experience**:
  - Font size controls (Smallest, Small, Medium, Large, Largest).
  - Material 3 Color Themes: Light, Warm Sepia, and Dark Mode.
  - Answers toggle in reading mode: "Hide until I tap" vs "Always show".
  - Table of Contents drawer for jumping between headings.
  - Automatic reading position saving & "Continue Reading" banner.
- **100% Offline & Private**:
  - Requires zero internet connection and requests zero device permissions.

## Architecture

- **UI Framework**: Jetpack Compose with Material 3 design system.
- **Architecture**: MVVM with unidirectional data flow and sealed screen state.
- **Build System**: Gradle 9.3.1 (Kotlin DSL), Android Gradle Plugin 9.1.1, Kotlin, targeting Android SDK 36.
