# SWFCopyValues
Used for Fallout 4 to fix 21:9 (or any other than 16:9) resolution problems

You will need .swf files that have already been changed (resolution wise) to look correct

For 21:9 I recommend using these: https://www.nexusmods.com/fallout4/mods/24630

The other thing that is required, is knowing which .swf files get replaced by a mod (that is not just there to fix resolution problems)

## Reason why we can't just use old mods that fix the resolution
Other mods (like Horizon, BetterConsole, ...) add additional functionalities in these mods (like tags / icons)

Unless the 21:9 resolution mod is very recently built (and has compatibility with these mods) it will remove these new functionalities

## How it works
There are three directories:
* Reference: The reference .swf files (for example from Truby9s mod)
* To Change: The .swf files that are loaded in your game, that have the correct functionality (but wrong resolution)
* Output: The output (as in new files with the functionality and correct resolution)

As a first step the program copies the header values for maximum y and x (which determines the resolution of the .swf file)

After that it goes through every frame of the .swf file and copies the matrix values 

(these have often changed translateX values due to the wider screen - sometimes some other stuff is changed as well)

## Additional Info
In this project I use the SWF-Library from https://github.com/jindrapetrik/jpexs-decompiler 

If you wish to run this locally you will need to grab the following libraries from the project linked above:
* avi
* cmykjpeg
* flashdebugger
* gif
* gnujpdf
* graphs
* jl1.0.1
* jpacker
* LZMA
* nellymoser
* sfntly
* ttf
