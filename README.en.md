# 🎬📚🎮 Library & Recommendation Platform (Android)

Android application developed as the **Final Degree Project (TFG)** for the Multiplatform Application Development (DAM) program.  
The project is a social platform where users can **review, rate, organize, and discover** entertainment content across different categories:

- 🎥 Movies  
- 📺 TV Shows  
- 🎮 Video Games  
- 🍥 Anime  
- 📖 Manga  
- 📚 Light Novels  

It also includes social features and an **artificial intelligence system** to automatically generate summaries of user reviews.

---

## 🚀 Main Features

- 🔎 **Content search** using external APIs:
  - TMDb (movies & TV shows)  
  - RAWG (video games)  
  - AniList (anime, manga, light novels)  

- 📌 **Content details**:  
  Complete information retrieved from APIs and stored in Firebase.

- 📝 **User reviews** with ratings and comments.

- 🤖 **AI-generated summaries**:  
  - The app generates review summaries with **GPT-3.5 (OpenAI)** once at least 3 reviews are available.  
  - Optimized to consume fewer tokens.  
  - Written in Spanish with the style of an **expert critic**.  

- 🌍 **Automatic translation into Spanish** with DeepL (for descriptions originally in English).  

- 👥 **Social features**:  
  Add friends, share preferences, chat, and create/share custom lists.  

---

## 🛠️ Technologies Used

- **Language**: Java (Android)  
- **Database**: Firebase Realtime Database  
- **AI**: OpenAI API (GPT-3.5)  
- **Translation**: DeepL API  
- **External APIs**: TMDb, RAWG, AniList  

---

## 📂 Project Structure

<code>/app/src/main/java/myvault
├── activities # Main activities
├── adapters   # Adapters for lists and views
├── models     # Data model classes
├── services   # Business logic
└── fragments  # Fragments to navigate through the main app features
</code>

---

## 📸 Screenshots

### Login
<img src="https://github.com/user-attachments/assets/167f46dc-7542-4f89-b25d-f8835d7e7725" width="300"/>

### Home
<img src="https://github.com/user-attachments/assets/b8fdf1ab-5dd0-4046-be75-7f0f94ae0262" width="300"/>

### Content
<img src="https://github.com/user-attachments/assets/087bdc1b-7b57-47b2-b7f1-9a9ae6c6f225" width="300"/>

### Content Details
<img src="https://github.com/user-attachments/assets/ce5adf1b-a33b-45a3-9bef-559326079fd3" width="250"/>
<img src="https://github.com/user-attachments/assets/3ca30bb4-5ce5-4243-a991-8022b931c66f" width="250"/>

### Reviews
<img src="https://github.com/user-attachments/assets/af9f1acd-1aad-4861-853e-fd50ae9b5bf7" width="300"/>

---

## 👤 Author

Developed by **Antonio Jesús Barroso Mesa** as the Final Degree Project (TFG) in **Multiplatform Application Development (DAM)**.  
📧 Contact: [ajesusbarrosocontacto@gmail.com]  
💼 LinkedIn: [www.linkedin.com/in/ajesusbarroso]  
