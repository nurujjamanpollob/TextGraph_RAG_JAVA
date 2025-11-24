# TextGraph RAG Java - Enhanced Version

TextGraph is a Java-based Retrieval-Augmented Generation (RAG) system for deep semantic analysis of unstructured text. Unlike traditional keyword search, it uses vector embeddings to understand context, allowing users to chat with local documents (TXT, PDF, MD, LOG) for summaries and answers without the risk of hallucinations.

This enhanced version includes significant improvements in security, performance, and maintainability over the original implementation.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Documentation](#documentation)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Configuration](#configuration)
- [Security Features](#security-features)
- [Performance Optimizations](#performance-optimizations)
- [Extending the System](#extending-the-system)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Semantic Code Search**: Find relevant code snippets using natural language queries
- **Project Indexing**: Automatically indexes codebases with smart delta updates
- **File Monitoring**: Real-time recursive file change detection and index updates
- **Persistent Storage**: Saves and loads indexes to/from disk
- **Multi-project Support**: Manage multiple projects with isolated contexts
- **Language Detection**: Automatic language detection and filtering
- **Smart Chunking**: Optimized document splitting for code files
- **Large File Handling**: Streaming processing for memory efficiency
- **External Configuration**: All parameters configurable via properties file
- **Standardized Logging**: Consistent logging throughout the application

## Architecture

The system consists of several key components:

1. **Main CLI Interface**: Command-line interface for user interaction
2. **Project Orchestrator**: Manages project lifecycle, indexing, and file monitoring
3. **Context Collector**: Handles vector embeddings and semantic search using LangChain4j
4. **File Watcher**: Monitors file system changes for real-time updates
5. **Utilities**: Helper classes for project analysis, file operations, path validation, logging, and configuration
6. **Models**: Data classes for project metadata and search results

## Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher
- 4GB RAM minimum (8GB recommended)

## Installation

### Quick Start

For a quick start, see our [Quick Start Guide](QUICK_START.md).

### Detailed Installation

For detailed platform-specific installation instructions, see our [Installation Guide](INSTALLATION_GUIDE.md).

### Clone the Repository

```bash
git clone https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA.git
cd TextGraph_RAG_JAVA
```

### Build the Project

```bash
./gradlew build
```

### Run the Application

```bash
./gradlew run
```

Or create a JAR and run it:

```bash
./gradlew jar
java -jar build/libs/TextGraph_RAG_JAVA-1.0-SNAPSHOT.jar
```

## Usage

After starting the application, you'll see a command prompt with the following available commands:

```
=== Intelligent RAG System v1.1 ===
Commands: load <path>, search <query>, exit
```

### Basic Workflow

1. **Load a Project**:
   ```
   load /path/to/your/project
   ```
   This will automatically:
   - Assign a project ID based on the folder name
   - Analyze the project structure
   - Create RAG metadata if it doesn't exist
   - Index all text files in the project

2. **Search the Project**:
   ```
   search How does the file watcher work?
   ```

### Example Session

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

[my-java-project]> search Find all classes that implement Runnable
📄 src/main/java/com/nurujjamanpollob/textenginejava/rag/io/FileWatcher.java
   --- public class FileWatcher implements Runnable {...

[my-java-project]> exit
[2025-04-05 10:30:15] [INFO] Shutting down.
```

## Documentation

Comprehensive documentation is available for different aspects of the system:

- [Quick Start Guide](QUICK_START.md) - Get up and running quickly
- [User Guide](USER_GUIDE.md) - Detailed instructions on using the system
- [Installation Guide](INSTALLATION_GUIDE.md) - Platform-specific installation instructions
- [Technical Documentation](TECHNICAL_DOCUMENTATION.md) - In-depth technical information
- [API Documentation](API_DOCUMENTATION.md) - Detailed API reference
- [Contributing Guide](CONTRIBUTING.md) - Guidelines for contributors
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md) - Summary of enhancements made
- [Final Implementation Report](FINAL_IMPLEMENTATION_REPORT.md) - Detailed implementation report
- [Changelog](CHANGELOG.md) - Version history and changes

## API Documentation

### Main Classes

#### `Main`
The entry point of the application that provides the CLI interface.

#### `ProjectOrchestrator`
Manages the lifecycle of projects including loading, indexing, and file monitoring.

**Key Methods:**
- `loadAndSyncProject(String projectId, String rootDirPath)`: Loads and indexes a project
- `searchProject(String projectId, String query)`: Searches a project using semantic queries
- `createRagMetadata(String projectId, String rootDirPath)`: Creates RAG metadata for a project
- `handleFileChange(String projectId, Path filePath, String eventType)`: Handles file system changes

#### `ContextCollector`
Manages vector embeddings and semantic search capabilities.

**Key Methods:**
- `updateFile(String projectId, String relativePath, String content)`: Indexes a file's content
- `updateFileStreaming(String projectId, String relativePath, Path filePath)`: Indexes large files using streaming
- `searchGrouped(String query, String projectId, int maxResults, double minScore)`: Performs semantic search on a project
- `saveIndexToDisk(String projectId, Path projectRoot)`: Saves embeddings to disk
- `loadIndexFromDisk(String projectId, Path projectRoot)`: Loads embeddings from disk

#### `FileWatcher`
Monitors file system changes recursively and notifies the orchestrator.

#### `ProjectProfile`
Holds metadata about a project including file counts and language breakdown.

#### `ProjectMetadata`
Stores file hashes and indexing timestamps for delta updates.

### Utility Classes

#### `ProjectInfoExtractor`
Analyzes project structure and provides statistical information.

#### `HashUtils`
Calculates file hashes for change detection using streaming to handle large files efficiently.

#### `PathValidator`
Validates and normalizes file paths to prevent directory traversal attacks.

#### `RagConfig`
Manages external configuration parameters loaded from `rag-config.properties`.

#### `RagLogger`
Provides standardized logging with colored output and timestamps.

#### `RagException`
Custom exception class for RAG-specific errors.

## Project Structure

```
TextGraph_RAG_JAVA/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nurujjamanpollob/textenginejava/rag/
│   │   │       ├── config/
│   │   │       │   └── RagConfig.java
│   │   │       ├── context/
│   │   │       │   └── ContextCollector.java
│   │   │       ├── exception/
│   │   │       │   └── RagException.java
│   │   │       ├── io/
│   │   │       │   └── FileWatcher.java
│   │   │       ├── model/
│   │   │       │   ├── ProjectMetadata.java
│   │   │       │   ├── ProjectProfile.java
│   │   │       │   └── RAGSearchResult.java
│   │   │       ├── utils/
│   │   │       │   ├── HashUtils.java
│   │   │       │   ├── PathValidator.java
│   │   │       │   ├── ProjectInfoExtractor.java
│   │   │       │   └── RagLogger.java
│   │   │       ├── Main.java
│   │   │       └── ProjectOrchestrator.java
│   │   └── resources/
│   │       └── rag-config.properties
│   └── test/
│       └── java/
│           └── RagSystemTest.java
├── JAR-LIBS/
├── .rag_data/ (generated per project)
├── build.gradle
├── settings.gradle
├── README.md
├── QUICK_START.md
├── USER_GUIDE.md
├── INSTALLATION_GUIDE.md
├── TECHNICAL_DOCUMENTATION.md
├── API_DOCUMENTATION.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── IMPLEMENTATION_SUMMARY.md
├── FINAL_IMPLEMENTATION_REPORT.md
└── LICENSE.txt
```

## How It Works

1. **Project Loading**: When a project is loaded, the system:
   - Analyzes the project structure using `ProjectInfoExtractor`
   - Creates/loads RAG metadata from `.rag_data/` directory
   - Performs smart synchronization by comparing file hashes
   - Indexes new or modified files using vector embeddings

2. **Semantic Indexing**: 
   - Uses `AllMiniLmL6V2EmbeddingModel` from LangChain4j for embeddings
   - Splits documents into chunks using `DocumentSplitters.recursive(1000, 150)`
   - Stores embeddings in an in-memory vector store with persistence capabilities
   - For large files, uses streaming processing to avoid memory issues

3. **Search Process**:
   - Converts query to embedding using the same model
   - Searches the vector store for similar embeddings
   - Groups results by file and returns relevant snippets
   - Uses configurable minimum score and maximum results parameters

4. **File Monitoring**:
   - Uses Java's `WatchService` to monitor file changes recursively
   - Automatically updates the index when files are modified
   - Handles file creation, modification, and deletion
   - Implements debouncing to avoid duplicate processing

5. **Persistence**:
   - Stores embeddings and metadata in the `.rag_data/` directory
   - Automatically loads previous indexes on project load
   - Maintains file hashes for efficient delta updates

## Configuration

The system can be configured using the `rag-config.properties` file in the `src/main/resources` directory:

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

Configuration parameters:
- `chunk.size`: Size of document chunks for embedding (default: 1000)
- `overlap.size`: Overlap between chunks (default: 150)
- `max.file.size`: Maximum file size for in-memory processing (default: 10MB)
- `min.score`: Minimum similarity score for search results (default: 0.6)
- `max.search.results`: Maximum number of search results to return (default: 15)

## Security Features

### Path Traversal Prevention
- All file paths are validated and normalized using `PathValidator`
- Prevents directory traversal attacks with proper path validation
- Absolute paths are checked against the base project directory

### Secure Exception Handling
- Custom `RagException` class for RAG-specific errors
- Standardized logging with `RagLogger` that doesn't expose sensitive information
- Sanitized error messages that don't reveal system internals

### Input Validation
- Comprehensive validation for all user-provided paths
- Suspicious pattern detection (e.g., "..", "~")
- Size limits for file processing to prevent resource exhaustion

## Performance Optimizations

### Recursive File Watching
- Monitors all subdirectories for real-time updates
- Efficiently handles file creation, modification, and deletion
- Implements debouncing to avoid duplicate processing

### Memory Efficiency
- Streaming processing for large files to prevent `OutOfMemoryError`
- Configurable maximum file size limits
- Efficient chunking algorithm for document processing

### Smart Synchronization
- Delta updates using file hashes to avoid reprocessing unchanged files
- Efficient file system scanning with ignored file type filtering
- Parallel processing of file updates where possible

## Extending the System

### Adding New File Types

To support additional file types:

1. Modify `ProjectInfoExtractor.isValidSourceFile()` to recognize the new extension
2. Ensure `TextFileDetector.isTextFile()` can handle the file type
3. Adjust chunking parameters in `ContextCollector.updateFile()` if needed

### Customizing Embedding Model

To use a different embedding model:

1. Replace the `AllMiniLmL6V2EmbeddingModel` in `ContextCollector`
2. Update dependencies in `build.gradle`
3. Adjust chunking parameters to match the new model's capabilities

### Adding External Vector Stores

To use a persistent vector database:

1. Replace `InMemoryEmbeddingStore` with a database-backed implementation
2. Update `saveIndexToDisk` and `loadIndexFromDisk` methods
3. Add required dependencies to `build.gradle`

### Custom Search Parameters

To modify search behavior:

1. Adjust `maxResults` and `minScore` in `ContextCollector.searchGrouped()`
2. Modify chunking parameters in `ContextCollector.updateFile()`
3. Add custom metadata fields to the embedding process

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a pull request

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for detailed information on our code of conduct and development process.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## Acknowledgments

- [LangChain4j](https://github.com/langchain4j/langchain4j) for Java-based LLM integration
- [Gson](https://github.com/google/gson) for JSON serialization
- [All-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) for embedding model