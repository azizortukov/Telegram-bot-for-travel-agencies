# Tour Company Telegram Bot

## Overview

This project is a Telegram bot designed for tour companies to streamline the communication between clients and company operators. It also helps CEOs manage and monitor client requests and operator interactions. The bot serves four types of users: clients, operators, editors, and admins.

## Features

### Client
- Browse categories of tours, such as United Arab Emirates, USA, and available services.
- View detailed tour packages with different dates and prices.
- Express interest in tour packages through an "Interested" button, triggering operator follow-up.

### Operator
- Manage client applications with two status buttons: "In Progress" and "Did Not Respond."
- View application details and update statuses with reasons for client disinterest.

### Editor
- Edit tour categories and packages.
- Manage operators by adding or removing them.
- Inspect application statuses.

### Admin
- Perform all editor functions.
- Generate monthly application reports in Excel format.
- Manage editors by adding or removing them.

## Technologies Used

- **Spring Boot Data**: For easy database management using JpaRepository.
- **Spring Boot Web**: To build the web application.
- **MongoDB**: To store data efficiently for small to medium-sized projects.
- **Apache POI**: To generate monthly reports for admins in Excel format.

### Why MongoDB?

MongoDB is lightweight and performs well for small to medium-sized projects like Telegram bots.

## Screenshots

### Client views:
  - <img src="/src/main/resources/screenshots/client_1.png" style="width:400px;">
  - <img src="/src/main/resources/screenshots/client_2.png" style="width:400px;">

### Operator views:
  - <img src="/src/main/resources/screenshots/operator_1.png" style="width:400px;">
  - <img src="/src/main/resources/screenshots/operator_2.png" style="width:400px;">

### Editor views:
  - <img src="/src/main/resources/screenshots/editor_1.png" style="width:400px;">
  - <img src="/src/main/resources/screenshots/editor_2.png" style="width:400px;">

### Admin views:
  - <img src="/src/main/resources/screenshots/admin_1.png" style="width:400px;">
  - <img src="/src/main/resources/screenshots/admin_2.png" style="width:400px;">

### Prerequisites
- Java 21
- MongoDB
- Telegram Bot API
- Apache POI

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/anas-ortukov/Tour-Company-Telegram-Bot.git
   cd tour-company-telegram-bot
   ```
2. Configure MongoDB connection in `application.yml`
3. Add telegram bot token in `application.yml`
4. Run the project
   
## Contributing

Contributions are welcome! Please fork the repository and create a pull request.
