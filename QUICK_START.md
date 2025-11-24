# TextGraph RAG Java Quick Start Guide

Get up and running with TextGraph RAG Java in just a few minutes.

## What is TextGraph RAG Java?

TextGraph RAG Java is a powerful tool that enables semantic search across your codebases and documents. Unlike traditional keyword-based search tools, TextGraph uses vector embeddings to understand the meaning and context of your queries, providing more relevant results.

## Prerequisites

Before you begin, ensure you have:
- Java 11 or higher installed
- Git (optional, for cloning the repository)
- Basic familiarity with command-line interfaces

Check your Java installation:
```bash
java -version
```

## Installation

### Option 1: Download Pre-built JAR (Easiest)

1. Download the latest release from [GitHub releases](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/releases)
2. Save the JAR file to a convenient location

### Option 2: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
   cd TextGraph_RAG_JAVA
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

## Running the Application

### With Pre-built JAR
```bash
java -jar TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

### From Source
```bash
./gradlew run
```

You should see:
```
=== Intelligent RAG System v1.1 ===
Commands: load <path>, search <query>, exit
(no-project)>
```

## Quick Tutorial

### Step 1: Load a Project

Load any directory you want to search:
```
load /path/to/your/project
```

For example:
```
load /home/user/my-java-project
```

The system will:
- Analyze the project structure
- Create necessary metadata files
- Index all text files in the project

Example output:
```
Creating metadata for my-java-project
Project: my-java-project
Path: /home/user/my-java-project
Files: 42 | Size: 2.1 MB
Top Languages: [java: 35, xml: 4, md: 2, gitignore: 1]

------------------------------------------------
Syncing project [my-java-project]...
Sync Complete. Updated: 42, Unchanged: 0
Loaded project: my-java-project
```

### Step 2: Search Your Project

Ask questions about your code:
```
search How does the file watcher work?
```

Or look for specific implementations:
```
search Examples of path validation
```

Example search result:
```
📄 src/main/java/com/nurujjamanpollob/textenginejava/rag/io/FileWatcher.java
   --- public class FileWatcher implements Runnable {...
```

### Step 3: Exit the Application

When you're done:
```
exit
```

## Example Workflow

Here's a complete example session:
```
=== Intelligent RAG System v1.1 ===
Commands: load <path>, search <query>, exit
(no-project)> load /home/user/sample-project
Creating metadata for sample-project
Project: sample-project
Path: /home/user/sample-project
Files: 15 | Size: 456 KB
Top Languages: [java: 10, md: 3, txt: 2]

------------------------------------------------
Syncing project [sample-project]...
Sync Complete. Updated: 15, Unchanged: 0
Loaded project: sample-project

[sample-project]> search Explain the project structure
📄 README.md
   --- # Sample Project This project demonstrates...

📄 src/main/java/com/example/Main.java
   --- /** * Main class that coordinates the application */ public class Main {...

[sample-project]> search How are files processed?
📄 src/main/java/com/example/FileProcessor.java
   --- /** * Processes files using streaming for memory efficiency */ public class FileProcessor {...

[sample-project]> exit
[2025-04-05 10:30:15] [INFO] Shutting down.
```

## Key Features

- **Semantic Search**: Find relevant code using natural language queries
- **Real-time Updates**: File changes are automatically indexed
- **Large File Support**: Efficiently handles large files using streaming
- **Multi-project Support**: Work with multiple projects
- **Configurable**: Adjust behavior through configuration files

## Next Steps

1. **Read the User Guide**: [USER_GUIDE.md](USER_GUIDE.md) for detailed usage instructions
2. **Explore Configuration**: [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md) for configuration options
3. **Check Examples**: Try different types of queries to see how semantic search works
4. **Review Documentation**: [README.md](README.md) for comprehensive information

## Getting Help

If you encounter any issues:
1. Check the [GitHub Issues](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/issues) page
2. Review the documentation files in the repository
3. Contact the maintainers through GitHub

## Contributing

Interested in improving TextGraph RAG Java?
1. Read [CONTRIBUTING.md](CONTRIBUTING.md)
2. Fork the repository
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see [LICENSE.txt](LICENSE.txt) for details.