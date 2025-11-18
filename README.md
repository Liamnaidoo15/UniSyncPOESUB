UniSync – Android Mobile Application

A modern Android application built using Kotlin, MVVM, and Jetpack libraries, designed for secure user access, cloud-based data synchronisation, real-time updates, and an intuitive user experience.
This application forms part of my PROG7314 POE, implementing all required features such as SSO login, REST API integration, offline mode, push notifications, multi-language support, and more.

## Table of Contents

Overview

Features

Technology Stack

System Architecture

API Integration

Offline Mode & Sync

Push Notifications

Multi-Language Support

Installation Guide

Screenshots

Automated Testing (GitHub Actions)

Release Notes

How AI Was Used

License

## Overview

UniSync is an Android application built to demonstrate backend integration, cloud features, secure authentication, and modern mobile development practices.
The app communicates with a custom-built REST API hosted online and stores synchronized data in a cloud database.

This project aligns with the requirements for the Programming 3D (PROG7314) Portfolio of Evidence, including:

App design

API creation & hosting

Authentication & cloud integration

GitHub workflows & testing

Video demonstration

Play Store preparation

## Features
- Authentication

Single Sign-On (SSO) with Firebase

Token-based authentication with hosted API

Biometric Authentication (fingerprint/face unlock)

- REST API Integration

Fully hosted API (Render.com)

CRUD operations

JWT-secured endpoints

Real-time synced data

- User Settings

Change personal preferences

Update language

Enable biometric login

Manage notifications

- Offline Mode + Sync

Local caching with RoomDB

Syncs automatically when reconnected

Conflict-safe strategy

- Push Notifications

Firebase Cloud Messaging (FCM)

Real-time alerts triggered from API

- Multi-Language Support

Includes at least 2 South African languages, for example:

English

isiZulu

Afrikaans (optional third language)

- Clean & Responsive UI

Material You components

Modern design patterns

Validations & error handling

## Technology Stack
Frontend

Kotlin

Jetpack Compose / XML Layouts

Android Jetpack (ViewModel, LiveData, Room, Navigation)

Backend

Node.js REST API

Firebase Admin SDK

PostgreSQL / Firestore / MongoDB (your choice)

Hosted on Render.com

Cloud

Firebase Authentication

Firebase Cloud Messaging

Firebase Storage (optional)

Tools

GitHub Actions

Postman

Android Studio

Gradle

## System Architecture
Android App (Kotlin)
        ↓
REST API (Node.js + Express)
        ↓
Database (PostgreSQL / Firestore)
        ↓
Firebase Services (Auth + Notifications)

## API Integration

The Android app communicates with the hosted API via Retrofit.

Base URL
https://<your-api-name>.onrender.com/api/

Example Endpoint
POST /auth/login
GET /user/settings
POST /sync/upload


Authentication uses:

Bearer JWT Token

Firebase user tokens for SSO

## Offline Mode & Sync

All user changes are stored locally when offline.

RoomDB caches data until connectivity is available.

A sync worker uploads queued data to the API.

## Push Notifications

Using Firebase Cloud Messaging (FCM):

API triggers notifications

App receives messages in real time

Channel support for Android 13+

## Multi-Language Support

Implemented through Android strings.xml files:

values/strings.xml           → English  
values-zu/strings.xml        → isiZulu  
values-af/strings.xml        → Afrikaans (optional)


The user selects the language in the Settings screen.

## Installation Guide
1. Clone Repo
git clone https://github.com/<your-username>/<repo-name>.git

2. Open in Android Studio
3. Add your API Base URL in Retrofit config
4. Build & Run on mobile device
## Screenshots

(Add your own here once the UI is finished.)

Example placeholders:

Login Screen

Dashboard

Settings Page

API Data Display

Offline Mode View

## Automated Testing (GitHub Actions)

This repository includes automated CI workflows:

Builds the Android app

Runs unit tests

Ensures API integration doesn’t break

Workflow file example:

.github/workflows/build.yml

## Release Notes
Version 1.0.0 – Final POE Submission

Added biometric authentication

Added offline mode with RoomDB

Added FCM push notifications

Added multi-language support

Completed API hosting & integration

Improved UI

Added GitHub Actions automated tests

Prepared assets for Play Store publishing

## How AI Was Used

AI tools such as ChatGPT were used responsibly for:

Debugging API deployment

Generating placeholder UI text

Assisting with documentation refinement

Providing examples of Kotlin code patterns

Generating JSON and environment variable formatting

All AI usage has been cited according to POE requirements.

