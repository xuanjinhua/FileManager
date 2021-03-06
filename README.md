# FileManager

FileManager is a GUI that allows to create new folders and import and organize your documents, as well as create new ones or edit existing ones, in any way you wish. You can easily delete files or folders that you do not want anymore as well. This GUI will also let you view any code (.java, .py, .ino, etc) file, as well as any .txt file.

IMPORTANT: You must have a folder "All-Documents" and a folder "Trash" in the same directory as this code!

- [Installation](#installation)
- [Usage](#usage)
- [Motivation](#motivation)
- [API](#api)
- [Known Issues](#known-issues)
- [Contributors](#contributors)
- [License](#license)

## Installation

    sudo apt-get install git-core
    git clone https://github.com/RPBruiser/FileManager.git

## Usage

From `FileManager` directory just run the following commands:

    javac FileManager.java FileEditor.java
    java FileManager

## Motivation

I wanted to create an application that would allow me acces to my files in a quick and simple way and displayed all of my most important files at the same time. I also wanted to build something that was easy to manipulate for my own needs, so I can adapt for the future if they change.

## API

Java 8: 
http://docs.oracle.com/javase/8/docs/api/

##Known Issues

On some Unix based OS `Files.createLink()` creates a copy rather than a link.

## Contributors

RPBruiser

## License

Apache Licensce 2.0
