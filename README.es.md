# 🎬📚🎮 Plataforma de Biblioteca y Reseñas (Android)

Aplicación Android desarrollada como **Trabajo de Fin de Grado (TFG)** en Desarrollo de Aplicaciones Multiplataforma (DAM).  
El proyecto consiste en una plataforma social donde los usuarios pueden **reseñar, puntuar, organizar y descubrir** contenido de entretenimiento en distintas categorías:

- 🎥 Películas  
- 📺 Series  
- 🎮 Videojuegos  
- 🍥 Anime  
- 📖 Manga  
- 📚 Novelas ligeras  

Incluye también funciones sociales y un sistema de **inteligencia artificial** para generar resúmenes automáticos de reseñas de usuarios.

---

## 🚀 Funcionalidades principales

- 🔎 **Búsqueda de contenidos** en APIs externas:
  - TMDb (películas y series)  
  - RAWG (videojuegos)  
  - AniList (anime, manga, novelas ligeras)  

- 📌 **Detalles de cada contenido**:  
  Información completa obtenida de las APIs y almacenada en Firebase.

- 📝 **Reseñas de usuarios** con puntuación y comentarios.

- 🤖 **IA de resúmenes automáticos**:  
  - La app genera un resumen de reseñas con **GPT-3.5 (OpenAI)** cuando hay al menos 3 reseñas.  
  - Optimizado para consumir pocos tokens.  
  - Redacción en español con estilo de **crítico experto**. 

- 🌍 **Traducción automática al español** con DeepL (para descripciones obtenidas en inglés).  

- 👥 **Funciones sociales**:  
  Añadir amigos, compartir gustos, chatear y crear y compartir listas personalizadas.

---

## 🛠️ Tecnologías utilizadas

- **Lenguaje**: Java (Android)  
- **Base de datos**: Firebase Realtime Database  
- **IA**: OpenAI API (GPT-3.5)  
- **Traducción**: DeepL API  
- **APIs externas**: TMDb, RAWG, AniList  

---

## 📂 Estructura del proyecto

<code>/app/src/main/java/myvault
├── activities # Actividades principales
├── adapters # Adaptadores para listas y vistas
├── models # Clases de modelo de datos
├── services # Lógica de negocio
└── fragments # Fragmentos para moverse a través de las funciones principales de la app
</code>

---

## 📸 Capturas de pantalla

### Login
<img src="https://github.com/user-attachments/assets/167f46dc-7542-4f89-b25d-f8835d7e7725" width="300"/>

### Inicio
<img src="https://github.com/user-attachments/assets/b8fdf1ab-5dd0-4046-be75-7f0f94ae0262" width="300"/>

### Contenido
<img src="https://github.com/user-attachments/assets/087bdc1b-7b57-47b2-b7f1-9a9ae6c6f225" width="300"/>

### Detalle de Contenido
<img src="https://github.com/user-attachments/assets/ce5adf1b-a33b-45a3-9bef-559326079fd3" width="250"/>
<img src="https://github.com/user-attachments/assets/3ca30bb4-5ce5-4243-a991-8022b931c66f" width="250"/>

### Reseñas
<img src="https://github.com/user-attachments/assets/af9f1acd-1aad-4861-853e-fd50ae9b5bf7" width="300"/>

---

## 👤 Autor

Desarrollado por **Antonio Jesús Barroso Mesa** como Trabajo de Fin de Grado (TFG) en **DAM**.  
📧 Contacto: [ajesusbarrosocontacto@gmail.com]  
💼 LinkedIn: [www.linkedin.com/in/ajesusbarroso]  
