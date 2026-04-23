# CST338-Project02 - Otter CS Trivia

JavaFX Collaborative Project for CST 338

## 🎮 Otter CS Trivia

**What is the application?**  
Otter CS Trivia is a trivia game that allows users to answer questions with a chance to earn their spot on the leaderboard. Users are separated into two different User groups: Admin and General.

User is first prompted to log in; if the user is not a current user, an error message will display. User is allowed to create an account, and their login is stored in a database.

## ✨ Features

### 🔐 Authentication System
- Secure login system with username/password authentication
- New user registration with account creation
- Role-based access control (Admin vs. General User)
- Session management with logout functionality

### 👑 Administrator Features
- Create and manage trivia questions
- View and manage the leaderboard with additional user information
- Ability to add questions to categories.

### 🎯 General User Features
- Answer trivia questions
- Track personal scores
- View leaderboard rankings
- Compete with other users

## 📋 Use Cases
#### Main Flow:
  1. User navigates to the Quiz Question scene from the main menu by selecting a quiz category
  2. System starts a quiz session
  3. System loads 10 random questions from the selected category (question + 4 answer options)
  4. User selects one of four answers for each question
  5. User submits answer and navigates to the next question
  6. System records question attempt in the database and associates questions with the current quiz attempt
  7. Repeat steps 2-4 until the quiz is finished
  8. If the quiz is finished, display the quiz results page with the option to return to the main menu

#### Alternate Flow:
- At step 2 of the main flow, if no questions are available, the system will display a message that no questions are available and to check back later
- User is presented with the option to return to the main menu

#### Main Flow:
  1. User navigates to Leaderboard from the main menu
  2. System queries the database for users ordered based on total score
  3. Displays top 10 users in a ranked table (rank, username, score)
  4. Displays the current user's New/Current highest score
  5. User presented with the option to return to the main menu or logout

#### Alternate Admin Flow:
- At step 2, check the user's role and if admin, wants to add question to quiz.

#### Alternate Flow (No scores exist):
- At step 3, if no users have any quiz attempts, the leaderboard displays "No attempts yet " + Category Name.

## 🛠️ Technical Stack

- **Language:** Java 17+
- **UI Framework:** JavaFX with FXML
- **Database:** SQLite
- **Build Tool:** Gradle / Maven
- **Version Control:** Git

## 📋 Prerequisites

- Java JDK 17 or higher
- JavaFX SDK
- SQLite JDBC Driver
- Gradle 7.0+ or Maven 3.6+

## 🚀 Installation

### Clone the Repository
```bash
git clone https://github.com/yourusername/CST338-Project02.git
cd CST338-Project02
