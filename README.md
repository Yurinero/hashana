![hashana-splash](https://github.com/user-attachments/assets/21c4aab4-cfef-4262-8fa3-b360504af00d)
# Hashana - A  fun desktop utility for hashing and password generation.
## The Why?
After completing a Java course, I was interested in trying to create a simple desktop application using the OpenJFX library. What started as a single window taking an input string to be hashed quickly got out of control as I kept trying to push myself further, constantly asking myself "How?" and "Can I?" and researching various topics.
It has been an interesting and rewarding journey for a beginner and I hope to expand on it in the future.

## The What?
You will find several Tabs providing the following functionality:

**Hash:** Input a string into the upper-right window, keeping spaces and new lines in mind. Choose the algorithm you wish to try/use and click ***Hash it!*** to perform the desired operation on the string. The right hand window provides some quick information about your chosen algorithm.

**Verify Checksum:** You may verify the checksum (hash) of a downloaded file. The checksum is usually either provided as a seperate file in the format *[filename].[extension].[hashalgorithm]* or *[filename].[hashalgorithm]* by the creator. The function will try to look for this combo upon selecting a file whose checksum you wish to verify. You may also parse in the Expected Hash. The checksum will be calculated and a Match/No Match message displayed.

**Create Checksum:** You may also create your own checksum by choosing the file whose hash you wish to calculate, choosing your algorithm and hitting Generate. The checksum file will be provided in the format *[filename].[extension].[hashalgoritm]* and saved to the same file directory as the chosen file.

**Password Generator:** A neat little password generator, you may choose a lenght from 6 to 32 characters, pick from different categories of characters and select whether you want to guarantee one from each category or fully randomize the result. Further you can add some extra entropy (randomness) if so desired.

**About:** A little thank you to the various open source libraries I used to create the application, display of the current version. You also get to see Hashana herself!

## Settings:
By clicking the gear icon in the top-right corner, next to the Minimize and Close buttons, you may modify a few settings:

**Buffer Size:** Controls the buffer used during the file hashing operations, to overcome the Java memory limit and perform the operation in a safer manner. Value is in kiloBytes. Larger will be faster, but will use more memory. Approach with caution!

**Maximum File Size:** Controls the maximum size of the files allowed for file hashing operations. Value is in megaBytes. I have personally not tested files larger than 5GB.

**Progress Interval:** Controls the refresh rate of the progress bars during file hashing operations. Value is in Milliseconds. Lower value may provide smoother updates but potentionally impact performance.

**Entropy Pool:** Controls the size of the pool used to add entropy during password generation. Value is represented in bytes.

**Splashscreen:** Controls whether the image of Hashana should be shown during the initialization of the application. Yes/No value.

**Styling:** Three possible styling options. Dark/Light are based on Catpuccin colour schemes, while Accessible aims to follow WCAG 2.1 standards.

## Credits
Libraries used:

 -  **JavaFX**: openjfx.io
 - **Google Guav**a: github.com/google/guava
 - **Jackson**: github.com/FasterXML/jackson
 -  **SLF4J & Logback**: www.slf4j.org & logback.qos.ch

Splashscreen implementation: 

**Edward Stephen Jr.** > https://coderscratchpad.com/creating-splash-screens-with-javafx/

## Building from Source

Will add soon(ish)!

## Licensing 
**GNU GENERAL PUBLIC LICENSE V3** is supplied with the application and applies to the source code and binaries.

## Issues / Pull Requests
If you find a bug, experience a critical error or have an idea for a improvement, feel free to open a GitHub Issue.

While I do not have any experience with collaboration yet, if you wish to submit an improvement you made or fixed an issue you spotted yourself, feel free to send it!
