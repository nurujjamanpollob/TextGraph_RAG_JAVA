# TextGraph RAG Java User Guide

This guide provides detailed instructions on how to use the TextGraph RAG Java system for semantic code search and document analysis.

## Table of Contents

- [Introduction](#introduction)
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Command Reference](#command-reference)
- [Project Management](#project-management)
- [Searching Documents](#searching-documents)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

## Introduction

TextGraph RAG Java is a powerful tool that enables semantic search across codebases and documents. Unlike traditional keyword-based search tools, TextGraph uses vector embeddings to understand the meaning and context of your queries, providing more relevant results.

The system works by:
1. Indexing your project files using semantic embeddings
2. Storing these embeddings for fast retrieval
3. Converting your search queries into embeddings
4. Finding the most semantically similar content

## System Requirements

- Java 11 or higher
- Gradle 7.0 or higher
- 4GB RAM minimum (8GB recommended for large projects)
- At least 100MB free disk space for index storage

## Installation

### Prerequisites

Ensure you have Java and Gradle installed on your system:

```bash
java -version
gradle -version
```

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
   cd TextGraph_RAG_JAVA
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew run
   ```

Alternatively, create a JAR and run it:
```bash
./gradlew jar
java -jar build/libs/TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

## Getting Started

After starting the application, you'll see a command prompt:

```
=== Intelligent RAG System v1.1 ===
Commands: load <path>, search <query>, exit
(no-project)>
```

### Basic Workflow

1. Load a project using the `load` command
2. Search within the project using the `search` command
3. Exit the application using the `exit` command

Example:
```
(no-project)> load /path/to/your/project
[my-project]> search Explain how the file watcher works
[my-project]> exit
```

## Command Reference

### load <path>

Loads and indexes a project from the specified path.

**Usage:**
```
load /path/to/project
```

**Behavior:**
- Automatically assigns a project ID based on the folder name
- Creates RAG metadata if it doesn't exist
- Analyzes the project structure
- Indexes all valid text files
- Starts file watching for real-time updates

**Example:**
```
(no-project)> load /home/user/my-java-project
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

### search <query>

Searches the currently loaded project using semantic search.

**Usage:**
```
search How does the file watcher work?
```

**Behavior:**
- Converts the query to a semantic embedding
- Searches the index for similar content
- Returns results grouped by file
- Shows relevant snippets with context

**Example:**
```
[my-project]> search Find all classes that implement Runnable
📄 src/main/java/com/nurujjamanpollob/textenginejava/rag/io/FileWatcher.java
   --- public class FileWatcher implements Runnable {...
```

### exit

Exits the application gracefully.

**Usage:**
```
exit
```

**Behavior:**
- Stops all file watchers
- Saves any pending index updates
- Exits the application

## Project Management

### Project Identification

Projects are identified by their folder name. When you load a project, the system automatically assigns an ID based on the directory name.

### Metadata Storage

Each project stores its metadata in a `.rag_data` directory within the project root:
- `metadata.json`: Contains file hashes and indexing timestamps
- `embeddings.json`: Contains the semantic embeddings for fast search

### File Watching

The system automatically monitors your project for changes:
- New files are indexed automatically
- Modified files are re-indexed
- Deleted files are removed from the index
- Changes are processed with a small delay to avoid duplicate processing

### Supported File Types

The system supports most text-based file formats:
- Source code files (java, py, js, cpp, etc.)
- Documentation files (md, txt, rst, etc.)
- Configuration files (xml, json, yaml, etc.)
- Log files

Binary files are automatically ignored.

## Searching Documents

### Search Quality

Search results are ranked by semantic similarity. The system uses a configurable minimum score threshold to filter out low-quality matches.

### Result Grouping

Results are grouped by file to make it easier to understand the context. Each file shows relevant snippets that match your query.

### Search Tips

1. **Be Specific**: More specific queries often yield better results
2. **Use Natural Language**: Phrase your queries as questions when possible
3. **Context Matters**: The system understands context, so include relevant details

**Good queries:**
- "How does the file watcher monitor subdirectories?"
- "Examples of using the PathValidator class"
- "Configuration options for chunk size"

**Less effective queries:**
- "file"
- "java"
- "class"

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
2. Rebuild the project: `./gradlew build`
3. Run the application

Changes to configuration parameters take effect when the application is restarted.

## Troubleshooting

### Common Issues

#### "Invalid directory" error when loading a project

**Cause**: The specified path doesn't exist or isn't a directory.

**Solution**: Verify the path exists and is a directory:
```bash
ls -la /path/to/project
```

#### "OutOfMemoryError" when processing large files

**Cause**: Attempting to process files larger than the configured limit.

**Solution**: 
1. Check file sizes: `ls -lh /path/to/large/files`
2. Increase `max.file.size` in `rag-config.properties`
3. Restart the application

#### No search results found

**Cause**: Query terms don't match indexed content or score threshold is too high.

**Solution**:
1. Try rephrasing the query using different words
2. Lower the `min.score` in `rag-config.properties`
3. Verify the project was indexed correctly

### Log Files

The system uses standardized logging with timestamps and levels:
- INFO: General operational messages
- WARN: Potential issues that don't stop execution
- ERROR: Problems that affect functionality
- DEBUG: Detailed information for troubleshooting

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

## Best Practices

### For Optimal Performance

1. **Project Size**: Keep projects to a reasonable size for better performance
2. **File Organization**: Organize files logically to make results more relevant
3. **Regular Updates**: Keep the system updated with the latest improvements
4. **Configuration Tuning**: Adjust configuration parameters based on your needs

### Security Considerations

1. **Path Validation**: The system validates all paths to prevent directory traversal
2. **File Access**: Only access projects you have permission to read
3. **Index Storage**: Index files are stored within the project directory
4. **Exception Handling**: Sensitive information is not exposed in error messages

### Index Maintenance

1. **Automatic Updates**: The system automatically updates indexes when files change
2. **Manual Refresh**: Use the `refresh` command to force re-indexing if needed
3. **Index Storage**: Indexes are stored in `.rag_data` directories
4. **Backup**: Consider backing up important indexes

## Advanced Usage

### Multiple Projects

You can work with multiple projects by loading them one at a time. The system maintains separate indexes for each project.

### Integration with Other Tools

The system's index files can potentially be integrated with other tools:
- Index files are stored in JSON format
- Embeddings use standard vector representations
- Metadata is stored in a structured format

### Custom Development

For developers wanting to extend the system:
1. Refer to the API Documentation
2. Follow the existing code patterns
3. Use the provided utility classes
4. Maintain consistency with the current architecture

## Getting Help

If you encounter issues not covered in this guide:

1. Check the [GitHub Issues](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/issues) page
2. Review the [Implementation Summary](IMPLEMENTATION_SUMMARY.md)
3. Examine the [Final Implementation Report](FINAL_IMPLEMENTATION_REPORT.md)
4. Contact the maintainers through GitHub

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.