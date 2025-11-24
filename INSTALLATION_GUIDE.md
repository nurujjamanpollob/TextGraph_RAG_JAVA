# TextGraph RAG Java Installation Guide

This guide provides detailed instructions for installing and setting up the TextGraph RAG Java system on various platforms.

## Table of Contents

- [System Requirements](#system-requirements)
- [Prerequisites](#prerequisites)
- [Installation Methods](#installation-methods)
  - [Building from Source](#building-from-source)
  - [Using Pre-built JAR](#using-pre-built-jar)
- [Platform-Specific Instructions](#platform-specific-instructions)
  - [Windows](#windows)
  - [macOS](#macos)
  - [Linux](#linux)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Updating](#updating)

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+, CentOS 7+)
- **Java**: Java 11 or higher
- **Memory**: 4GB RAM
- **Disk Space**: 100MB available space (plus space for project indexes)

### Recommended Requirements
- **Operating System**: Latest stable versions of Windows, macOS, or Linux
- **Java**: Java 17 or higher
- **Memory**: 8GB RAM (for large projects)
- **Disk Space**: 1GB available space (for large project indexes)

## Prerequisites

### Java Installation

#### Check Java Version
```bash
java -version
javac -version
```

If Java is not installed or is an unsupported version, follow the instructions below for your platform.

#### Windows
1. Download OpenJDK from [Adoptium](https://adoptium.net/)
2. Run the installer and follow the prompts
3. Add Java to your PATH environment variable

#### macOS
Using Homebrew:
```bash
brew install openjdk@17
```

Or download from [Adoptium](https://adoptium.net/)

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

#### Linux (CentOS/RHEL/Fedora)
```bash
sudo yum install java-17-openjdk-devel
# Or for newer versions:
sudo dnf install java-17-openjdk-devel
```

### Gradle Installation (for building from source)

#### Check Gradle Version
```bash
gradle -version
```

#### Windows
Using Scoop:
```bash
scoop install gradle
```

Or download from [Gradle website](https://gradle.org/releases/)

#### macOS
Using Homebrew:
```bash
brew install gradle
```

#### Linux
Using package manager:
```bash
# Ubuntu/Debian
sudo apt install gradle

# CentOS/RHEL/Fedora
sudo yum install gradle
# Or for newer versions:
sudo dnf install gradle
```

## Installation Methods

### Building from Source

This method gives you the latest version and allows for customization.

#### 1. Clone the Repository
```bash
git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
cd TextGraph_RAG_JAVA
```

If Git is not installed:
- **Windows**: Download from [Git for Windows](https://git-scm.com/download/win)
- **macOS**: `brew install git`
- **Linux**: `sudo apt install git` (Ubuntu/Debian) or `sudo yum install git` (CentOS/RHEL)

#### 2. Build the Project
```bash
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

#### 3. Run the Application
```bash
./gradlew run
```

On Windows:
```bash
gradlew.bat run
```

### Using Pre-built JAR

This method is simpler but may not include the latest features.

#### 1. Download the JAR
Download the latest release from the [GitHub releases page](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/releases).

#### 2. Run the Application
```bash
java -jar TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

## Platform-Specific Instructions

### Windows

#### Prerequisites Installation
1. Download and install Java from [Adoptium](https://adoptium.net/)
2. Download and install Git from [Git for Windows](https://git-scm.com/download/win)
3. Download Gradle from [Gradle website](https://gradle.org/releases/) or use Scoop:
   ```bash
   scoop install gradle
   ```

#### Building from Source
```cmd
git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
cd TextGraph_RAG_JAVA
gradlew.bat build
gradlew.bat run
```

#### Environment Variables
Ensure these are set in your PATH:
- Java bin directory (e.g., `C:\Program Files\Java\jdk-17\bin`)
- Git bin directory (e.g., `C:\Program Files\Git\bin`)
- Gradle bin directory (if installed manually)

### macOS

#### Prerequisites Installation
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java, Git, and Gradle
brew install openjdk@17 git gradle
```

#### Building from Source
```bash
git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
cd TextGraph_RAG_JAVA
./gradlew build
./gradlew run
```

#### Environment Setup
Add to your shell profile (`.zshrc`, `.bash_profile`, etc.):
```bash
export JAVA_HOME=/usr/libexec/java_home -v 17
```

### Linux

#### Ubuntu/Debian
```bash
# Update package list
sudo apt update

# Install prerequisites
sudo apt install openjdk-17-jdk git gradle

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

#### CentOS/RHEL/Fedora
```bash
# Install prerequisites
sudo yum install java-17-openjdk-devel git gradle
# Or for newer versions:
sudo dnf install java-17-openjdk-devel git gradle

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

#### Building from Source
```bash
git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
cd TextGraph_RAG_JAVA
./gradlew build
./gradlew run
```

## Configuration

### Configuration File

The system can be configured using `rag-config.properties` in the `src/main/resources` directory:

```properties
# Document splitting parameters
chunk.size=1000
overlap.size=150

# File processing limits
max.file.size=10485760

# Search parameters
min.score=0.6
max.search.results=15
```

### Configuration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| chunk.size | 1000 | Size of document chunks for embedding |
| overlap.size | 150 | Overlap between chunks |
| max.file.size | 10485760 (10MB) | Maximum file size for in-memory processing |
| min.score | 0.6 | Minimum similarity score for search results |
| max.search.results | 15 | Maximum number of search results to return |

### Modifying Configuration

To modify the configuration:

1. Edit `src/main/resources/rag-config.properties`
2. Rebuild the project: `./gradlew build` (when building from source)
3. Run the application

## Running the Application

### From Source

#### Development Mode
```bash
./gradlew run
```

#### Create and Run JAR
```bash
./gradlew jar
java -jar build/libs/TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

### From Pre-built JAR
```bash
java -jar TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

### Command Line Options

The application currently doesn't support command line arguments and runs in interactive mode. Future versions may add command line options.

## Verification

### Successful Build
After running `./gradlew build`, you should see:
```
BUILD SUCCESSFUL
```

### Successful Run
After starting the application, you should see:
```
=== Intelligent RAG System v1.1 ===
Commands: load <path>, search <query>, exit
(no-project)>
```

### Test Functionality
1. Load a project:
   ```
   load /path/to/any/directory
   ```

2. Search within the project:
   ```
   search some query about the project
   ```

3. Exit the application:
   ```
   exit
   ```

## Troubleshooting

### Common Issues

#### "JAVA_HOME not set" Error
**Problem**: The system cannot find the Java installation.

**Solution**:
- **Windows**: Set JAVA_HOME environment variable to your Java installation directory
- **macOS/Linux**: Add `export JAVA_HOME=/path/to/java` to your shell profile

#### "Gradle not found" Error
**Problem**: Gradle is not installed or not in PATH.

**Solution**:
- Install Gradle using your system's package manager or download from the Gradle website
- Ensure Gradle bin directory is in your PATH

#### "OutOfMemoryError" During Build
**Problem**: Insufficient memory for the build process.

**Solution**:
- Increase heap size: `export GRADLE_OPTS="-Xmx2g"`
- Close other memory-intensive applications

#### "Permission denied" Error
**Problem**: Insufficient permissions to execute files.

**Solution**:
- **Linux/macOS**: Make scripts executable: `chmod +x gradlew`
- Run with appropriate permissions

#### Build Failures
**Problem**: Dependency resolution or compilation errors.

**Solution**:
1. Clean the build: `./gradlew clean`
2. Refresh dependencies: `./gradlew --refresh-dependencies build`
3. Check internet connection for dependency downloads

### Logging

The application uses standardized logging with different levels:
- **INFO**: General operational messages
- **WARN**: Potential issues
- **ERROR**: Problems affecting functionality
- **DEBUG**: Detailed troubleshooting information

Logs are displayed in the console with color coding:
- Blue: INFO messages
- Yellow: WARN messages
- Red: ERROR messages
- Green: DEBUG messages

### Performance Issues

If the system seems slow:

1. **Large Projects**: Indexing large projects takes time initially
2. **Memory Constraints**: Ensure sufficient RAM is available
3. **Disk I/O**: Check if disk performance is adequate
4. **Configuration**: Verify configuration parameters are appropriate

## Updating

### From Source

To update to the latest version:

1. Navigate to the project directory
2. Pull the latest changes:
   ```bash
   git pull origin master
   ```
3. Rebuild the project:
   ```bash
   ./gradlew clean build
   ```

### Preserving Configuration

When updating, your configuration files will be preserved if:
- You're using the standard configuration file location
- You haven't modified the build structure

If you have custom configurations, back them up before updating:

```bash
cp src/main/resources/rag-config.properties rag-config.properties.backup
```

After updating, restore your configuration:
```bash
cp rag-config.properties.backup src/main/resources/rag-config.properties
```

## Uninstalling

### Removing Source Installation

To completely remove the source installation:

1. Delete the project directory:
   ```bash
   rm -rf TextGraph_RAG_JAVA
   ```

2. Remove any created indexes by deleting `.rag_data` directories in your projects

### Removing Pre-built JAR

To remove the pre-built JAR installation:

1. Delete the JAR file
2. Remove any created indexes by deleting `.rag_data` directories in your projects

## Support

If you encounter issues not covered in this guide:

1. Check the [GitHub Issues](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/issues) page
2. Review the [User Guide](USER_GUIDE.md)
3. Examine the [Technical Documentation](TECHNICAL_DOCUMENTATION.md)
4. Contact the maintainers through GitHub

## Contributing

If you'd like to contribute to the project:

1. Follow the installation guide to set up a development environment
2. Read [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines
3. Submit pull requests with improvements

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.