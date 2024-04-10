# FrameSnap

**Presented by:**

- Gloria Marinelli, 2054014

## Index

- [Introduction](#introduction)
- [The application](#the-application)
  - [Paint](#paint)
  - [Social Interactions](#social-interactions)
  - [Maps](#maps)
- [Prerequisites](#prerequisites)
- [Installation and Launch](#installation-and-launch)

## Introduction

Welcome to **FrameSnap**! This is a mobile application in which you'll find the possibility to add your friends, trying a texture or a color on the wall using the image processing techniques. You just need an Android smartphone, an account and an Internet connection!

The architecture of the application is composed of:

- A **frontend node**, that is a mobile application coded in Kotlin;
- A **backend node**, that is a RESTful web service developed in python, through the Flask framework;
- A **database node**, that is a MySQL DBMS.

The backend node and the database node run on the [PythonAnywhere](https://www.pythonanywhere.com/) web hosting platform; this allows the user to run only the frontend node from its Android smartphone. See [Installation and Launch](#installation-and-launch) section for further details.

## The application

**FrameSnap** is a mobile application in which you'll find the possibility to add your friends, trying a texture or a color on the wall using the image processing techniques. Once you have installed and opened the application (see [Installation and Launch](#installation-and-launch) and [Prerequisites](#prerequisites) sections for further details), you have to create an account in order to have access to all the provided features. Once you have created an account, you'll have to login into the application.

Once the login is performed, you'll be presented the **homepage** of the application: this screen contains all the **texture** that are available in the system for try them. On the top part of the screen, there is a **notification** icon, through which the user will be able to see its notifications that will appear upon the receival of a friendship request. On the bottom part of the screen, there is a **navigation bar**, through which the user will be able to navigate throughout the application. 

The first icon of this navigation bar is the default one (**homepage**); i.e., selecting this icon will redirect the user to the homepage of the application.

The second icon of the navigation bar is the **friends** icon, and upon the click of such an icon, the user will be redirected to a screen showing all the users that are currently signed up to the system. The user will be able also to search for a specific user, given its username. By clicking on one username among all those shown, the user will be able to visit the profile of the selected user.

The thirt icon allows the user to visit his/her own **profile****. Moreover, by selecting the settings icon in this screen, the user will be able to logout, to modify his/her own account and/or to delete his/her own account.

The fourth icon allows the user to see its current **location** and the location of the shops, based on a Google Maps API.

### Paint

The **Paint** feature is the most important feature of our application. It is based on **OpenCV** that is the world's biggest computer vision library. Basically, when the user selects a paint in the homepage, the application will open the camera, the user can take a photo of a wall and perform some action by clicking on the top menu composed by **Final Image, Process Image, Take Photo, Open Gallery, Choose Paint, Choose Texture**. The application will process the image and will show the final image with the paint/texture on the wall in a 2x3 layout.

### Social Interactions

The **Social Interaction** is when the user visits the profile of another user, (s)he could decide to send to the other user a friendship request; the other user can decide either to accept or refuse such a friendship request. 

### Maps

The **Map** feature allows the user to see its current location and the location of the shops, based on a [Google Map API](https://developers.google.com/maps/documentation/android-sdk). The user can also see the distance between his/her current location and the shop.

## Prerequisites

In order to correctly launch the application, the user must have:

- An Android smartphone with Android version 7.0 or higher;
- An Internet connection;
- At least 100MB of free space.

## Installation and Launch

The application can be downloaded by clicking this ******. Once you have downloaded and installed the application, ensure you're in the following setting **before executing it**:
