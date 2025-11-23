# TextGraph RAG Java

TextGraph is a Java-based Retrieval-Augmented Generation (RAG) system for deep semantic analysis of unstructured text. Unlike traditional keyword search, it uses vector embeddings to understand context, allowing users to chat with local documents (TXT, PDF, MD, LOG) for summaries and answers without the risk of hallucinations.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Extending the System](#extending-the-system)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Semantic Code Search**: Find relevant code snippets using natural language queries
- **Project Indexing**: Automatically indexes codebases with smart delta updates
- **File Monitoring**: Real-time file change detection and index updates
- **Persistent Storage**: Saves and loads indexes to/from disk
- **Multi-project Support**: Manage multiple projects with isolated contexts
- **Language Detection**: Automatic language detection and filtering
- **Smart Chunking**: Optimized document splitting for code files

## Architecture

The system consists of several key components:

1. **Main CLI Interface**: Command-line interface for user interaction
2. **Project Orchestrator**: Manages project lifecycle, indexing, and file watching
3. **Context Collector**: Handles vector embeddings and semantic search using LangChain4j
4. **File Watcher**: Monitors file system changes for real-time updates
5. **Utilities**: Helper classes for project analysis and file operations

## Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher
- 4GB RAM minimum (8GB recommended)

## Installation

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
=== Intelligent RAG System ===
Commands:
  load <path>                -> Load project from path (Auto-ID)
  load <project_id> <path>   -> Load/Index a project with specific ID
  create <project_id> <path> -> Create RAG metadata manually
  use <project_id>           -> Switch active context
  refresh                    -> Force re-scan of current project
  search <query>             -> Search code
  exit
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

3. **Switch Projects** (if you have multiple):
   ```
   use project_id
   ```

### Example Session

```
(no-project)> load /home/user/my-java-project
Inferred Project ID: my-java-project
Analyzing project structure...
Project: my-java-project
Path: /home/user/my-java-project
Files: 42 | Size: 2.1 MB
Top Languages: [java: 35, xml: 4, md: 2, gitignore: 1]

------------------------------------------------
Syncing project [my-java-project]...
Sync Complete. Updated/Added: 42, Unchanged: 0
Successfully loaded. Active Project ID: my-java-project

[my-java-project]> search Find all classes that implement Runnable
📄 FILE: src/main/java/TaskProcessor.java
   --- Snippet ---
   public class TaskProcessor implements Runnable {
       // implementation details
   }

[my-java-project]> exit
Goodbye.
```

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
- `searchGrouped(String query, String projectId)`: Performs semantic search on a project
- `saveIndexToDisk(String projectId, Path projectRoot)`: Saves embeddings to disk
- `loadIndexFromDisk(String projectId, Path projectRoot)`: Loads embeddings from disk

#### `FileWatcher`
Monitors file system changes and notifies the orchestrator.

#### `ProjectProfile`
Holds metadata about a project including file counts and language breakdown.

#### `ProjectMetadata`
Stores file hashes and indexing timestamps for delta updates.

### Utility Classes

#### `ProjectInfoExtractor`
Analyzes project structure and provides statistical information.

#### `HashUtils`
Calculates file hashes for change detection.

## Project Structure

```
TextGraph_RAG_JAVA/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nurujjamanpollob/textenginejava/rag/
│   │   │       ├── context/
│   │   │       │   └── ContextCollector.java
│   │   │       ├── io/
│   │   │       │   └── FileWatcher.java
│   │   │       ├── model/
│   │   │       │   ├── ProjectMetadata.java
│   │   │       │   ├── ProjectProfile.java
│   │   │       │   └── RAGSearchResult.java
│   │   │       ├── utils/
│   │   │       │   ├── HashUtils.java
│   │   │       │   └── ProjectInfoExtractor.java
│   │   │       ├── Main.java
│   │   │       └── ProjectOrchestrator.java
│   │   └── resources/
│   └── test/
├── JAR-LIBS/
├── .rag_data/ (generated per project)
├── build.gradle
├── settings.gradle
└── README.md
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

3. **Search Process**:
   - Converts query to embedding using the same model
   - Searches the vector store for similar embeddings
   - Groups results by file and returns relevant snippets

4. **File Monitoring**:
   - Uses Java's `WatchService` to monitor file changes
   - Automatically updates the index when files are modified
   - Handles file creation, modification, and deletion

5. **Persistence**:
   - Stores embeddings and metadata in the `.rag_data/` directory
   - Automatically loads previous indexes on project load
   - Maintains file hashes for efficient delta updates

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [LangChain4j](https://github.com/langchain4j/langchain4j) for Java-based LLM integration
- [Gson](https://github.com/google/gson) for JSON serialization
- [All-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) for embedding model