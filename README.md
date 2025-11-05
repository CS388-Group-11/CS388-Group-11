Milestone 1 - StudySync (Unit 7)

## Table of Contents
- [Overview](#overview)
- [Product Spec](#product-spec)
- [Wireframes](#wireframes)
- [BONUS: Digital Wireframes & Mockups](#bonus-digital-wireframes--mockups)
- [BONUS: Interactive Prototype](#bonus-interactive-prototype)

## Overview

### Description
StudySync is a mobile app designed to help college students connect and form study groups. Users can find existing groups for their courses at their university or create their own, setting a time, location, and topic to invite others.

### App Evaluation
[Evaluation of your app across the following attributes]
* **Category:** Education / Social
* **Mobile:** Medium. This could be a website, but it becomes uniquely mobile if you use location ("find groups studying nearby") and push notifications ("a new group for your class just posted!").
* **Story:** "Stop studying alone. Find a group for your exact class."
* **Market:** The market is "all college students," which is a huge and well-defined audience.
* **Habit:** Use is seasonal. It would be heavily used around midterms and finals, but less so at the start of a semester.
* **Scope:** The core app (profiles, create/join groups, filter by school/class) seems very doable.

## Product Spec

### 1. User Features (Required and Optional)
**Required Features**
* **R1: User Authentication:** User can sign up for an account (ideally verifying their university) and log in.
* **R2: Create Study Group:** User can create a new public group listing, including **Course Code** (e.g., CS-490), **Topic** (e.g., "Midterm Prep"), **Time/Date**, and **Location** (e.g., "Library 2nd Floor" or "Discord Link").
* **R3: Search/View Groups:** User can see a list of all active groups for their university, and can filter this list by course code.

**Optional Features**
* **O1: Group Chat:** A simple, built-in chat screen for each group so members can coordinate.
* **O2: Push Notifications:** User receives a notification when someone joins their group, or when a new group is created for a course they've "favorited."
* **O3: Join/Leave Group:** User can join a group from the list, and also leave a group they've joined.

### 2. Screen Archetypes
* **Login/Sign Up Screen:** User creates an account or logs in. [R1]
* **Dashboard (Search) Screen:** The main screen where a user can search/filter for study groups by course. [R3]
* **Create Group Screen:** A form for a user to fill out the details for a new group. [R2]
* **My Groups Screen:** A list of all groups the user has either created or joined. [R3]
* **Group Detail Screen:** Shows all information for one specific group (topic, time, member list) and has the "Join" button. [R3]
* **(Optional) Group Chat Screen:** A chat interface for a group the user has joined. [O1]

### 3. Navigation
**Tab Navigation (Tab to Screen)**
* **"Search" Tab:** Navigates to the **Dashboard (Search) Screen**.
* **"My Groups" Tab:** Navigates to the **My Groups Screen**.
* **"Profile" Tab:** Navigates to the **(Optional) Profile Screen**.

**Flow Navigation (Screen to Screen)**
* **Login/Sign Up Screen** -> (on success) -> **Dashboard (Search) Screen**
* **Dashboard (Search) Screen** -> (tap on a group) -> **Group Detail Screen**
* **Dashboard (Search) Screen** -> (tap "+" icon) -> **Create Group Screen**
* **Group Detail Screen** -> (tap "Join") -> **My Groups Screen** (or **Group Chat Screen**)
* **Create Group Screen** -> (tap "Create") -> **My Groups Screen**
* **My Groups Screen** -> (tap on a group) -> **Group Detail Screen** (or **Group Chat Screen**)


## Wireframes

<img width="632" height="747" alt="image" src="https://github.com/user-attachments/assets/0eb9c9dc-df90-479d-b5cf-7a96402b556a" />


## [BONUS] Digital Wireframes & Mockups

https://www.figma.com/design/KHHRsBzn2f3zIEYtskbwGO/Study-Sync?node-id=0-1&p=f&t=xKBeJ1kG4dJgQAI5-0

## [BONUS] Interactive Prototype

![studysyncgif](https://github.com/user-attachments/assets/2c31cdbe-d262-4779-947b-8641ce799d39)
