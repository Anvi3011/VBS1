# 💰 Virtual Banking System (VBS)

## 👩‍💻 Author
**Anvi Patil**

---

## 📌 Project Overview
Virtual Banking System (VBS) is a full-stack web application that simulates basic banking operations like account management, deposits, withdrawals, transfers, and transaction tracking.

This project demonstrates real-world banking functionalities with both **User** and **Admin** roles.

---

## 🚀 Features

### 👤 User Features
- User Registration & Login
- Deposit Money
- Withdraw Money
- Transfer Funds
- View Account Balance
- View Transaction History (Passbook)
- Update Profile

### 🛠️ Admin Features
- View all users
- Add new users
- Delete users
- View system statistics (total users, total balance, etc.)
- View transaction history

---

## 🏗️ Tech Stack

### Frontend
- HTML
- CSS
- JavaScript

### Backend
- Java (Spring Boot)

### Database
- MySQL

---

## 📂 Project Structure
Frontend:

home.html
login.html
signup.html
dashboard.html
passbook.html
history.html
admin.html

Backend:

Controller (UserController, TransactionController, HistoryController)
Model (User, Transaction, History)
Repository (UserRepo, TransactionRepo, HistoryRepo)
DTO (TransferDto, TransactionDto)
