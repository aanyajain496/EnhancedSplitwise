Enhanced Splitwise – Smart Expense Sharing Platform

A FinanceTech application designed to simplify shared expense management by helping users create groups, manage members, and track shared expenses. The project combines an Android application with a Node.js backend and PostgreSQL database, with OCR-based receipt text recognition to make expense entry easier.

🚀 Features Implemented
- User Authentication: User registration and login using the backend API.
- Group Management: Create and view expense-sharing groups.
- Member Management: Add registered users to groups.
- Expense Tracking: Connect the Android application to backend expense APIs.
- Receipt Text Recognition: Use Google ML Kit to recognize text from receipt images.
- Database Integration: Store and manage application data using PostgreSQL.
- REST API: Handle application requests through Node.js and Express.js.

Planned Enhancements
- Split expenses equally, unequally, by percentage, or by exact amounts.
- Display detailed group information and member lists.
- Calculate individual balances and outstanding amounts.
- Implement settlements between group members.
- Extract structured expense details automatically from scanned receipts.
- Add spending insights and monthly expense summaries.

🛠️ Tech Stack

| Component | Technology |
| Android Frontend | Kotlin, XML |
| Backend | Node.js, Express.js |
| Database | PostgreSQL 18 |
| Receipt Text Recognition | Google ML Kit |
| API Communication | REST API |
| Development Environment | Android Studio, Visual Studio Code |

🏗️ System Architecture

The application follows a client-server architecture:

1. Android Application: Provides the user interface for authentication, groups, and expenses.
2. Backend API: Processes requests and manages business logic using Node.js and Express.js.
3. PostgreSQL Database: Stores users, groups, memberships, expenses, and related records.
4. Google ML Kit: Recognizes text in receipt images to support expense entry.

⚙️ Getting Started

Prerequisites

Install the following tools:

- [Android Studio](https://developer.android.com/studio)
- [Node.js](https://nodejs.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Visual Studio Code](https://code.visualstudio.com/) (optional)

1. Clone the Repository
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd <YOUR_PROJECT_FOLDER>

Replace the placeholders with your actual repository URL and project folder name.

2. Set Up the Backend

Open a terminal and navigate to the backend directory:
cd backend
npm install
npm run dev

Configure the required database connection and authentication environment variables according to the backend configuration before starting the server.

The development backend runs at: http://localhost:3001

Health-check endpoint: http://localhost:3001/api/v1/health

3. Configure the Android Application

1. Open the Android project in Android Studio.
2. Allow Gradle synchronization to finish.
3. Ensure the backend is running.
4. For the Android Emulator, configure the API base URL as: http://10.0.2.2:3001


5. Run the application on an emulator or compatible Android device.

Note: The emulator address `10.0.2.2` accesses the development computer's localhost. A physical Android device requires the computer's reachable local IP address and appropriate network configuration.

🔌 API Overview

The backend exposes versioned REST API endpoints.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a user |
| POST | `/api/v1/auth/login` | Authenticate a user |
| GET | `/api/v1/groups` | Retrieve groups |
| POST | `/api/v1/groups` | Create a group |
| GET | `/api/v1/groups/:id` | Retrieve group details |
| POST | `/api/v1/groups/:id/members` | Add a group member |
| GET | `/api/v1/expenses` | Retrieve expenses |
| POST | `/api/v1/expenses` | Create an expense |

Protected endpoints require authentication. Refer to the backend implementation for request formats and authorization requirements.

🗄️ Database

The PostgreSQL database includes tables for:

- Users and authentication
- Groups and group memberships
- Expenses and expense splits
- Categories and monthly summaries
- Notifications
- Refresh tokens
- Settlements

🔒 Security Notes

- Keep database credentials, JWT secrets, and other sensitive configuration out of version control.
- Use environment variables for secrets.
- Do not commit real user credentials or production database details.
- Use HTTPS and appropriate network security settings before deploying the application publicly.

🎯 Project Objective

The objective of Enhanced Splitwise is to make shared financial management more organized, transparent, and convenient. By combining group-based expense tracking, database-backed services, and receipt text recognition, the project lays the foundation for a smarter expense-sharing platform.

👩‍💻 Project Status

Development in progress. Authentication, group creation, member management, backend connectivity, and receipt text recognition have been implemented. Complete expense-splitting calculations, balance tracking, settlements, and advanced receipt data extraction remain planned enhancements.
