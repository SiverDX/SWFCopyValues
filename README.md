# SWFCopyValues
Used for Fallout 4 to fix 21:9 resolution problems

Required are the files that are supposed to be changed (from 16:9 to 21:9) and reference .swf files

Usually one can just use the .swf files form this mod: https://www.nexusmods.com/fallout4/mods/24630, it should cover everything

## Reason why we can't just use old mods that fix the resolution
Other mods (like Horizon, BetterConsole, ...) add additional functionalities in these mods (like tags / icons)

Unless the 21:9 resolution mod is very recently built (and has compatibility with these mods) it will remove these new functionalities

## How it works
There are three directories:
* The files that are loaded in your game, that have the correct functionality (but wrong resolution)
* The reference files (for example from Truby9s mod)
* The output

As a first step the program copies the header values for maximum y and x (which determines the resolution of the .swf file)

After that it goes through every frame of the .swf file and copies the matrix values 

(these have often changed translateX values due to the wider scren)

## Additional Info
In this project I use the SWF-Library from https://github.com/jindrapetrik/jpexs-decompiler 
