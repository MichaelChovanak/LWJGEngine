Code written by Michael Chovanak, 2020

This project uses the Light Weight Java Game Library (LWJGL) version 3.2.3.

This render engine uses real-time raymarching to render a scene. Raymarching works by casting a "ray" for each pixel on the screen in the direction the camera is facing.
We then use a distance function to calculate how close the closest object is to the origin of the ray. we then "march" the ray forwards until the distance function
returns a very small number, indicating the ray has hit an object. The code then reads the properties of the object hit to calculate the corresponding pixel's color.
This is an expensive process, so this code utilizes compute shaders so that each pixel calculation is performed in parallel on the GPU. 

The original purpose of this project was to gain experience with LWJGL, compute shaders, and raymarching.
