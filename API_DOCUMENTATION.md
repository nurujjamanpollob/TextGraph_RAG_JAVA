# TextGraph RAG Java API Documentation

This document provides detailed information about the public APIs available in the TextGraph RAG Java system.

## Table of Contents

- [Overview](#overview)
- [Core Classes](#core-classes)
- [Configuration Classes](#configuration-classes)
- [Model Classes](#model-classes)
- [Utility Classes](#utility-classes)
- [Exception Classes](#exception-classes)
- [Usage Examples](#usage-examples)

## Overview

The TextGraph RAG Java API provides programmatic access to semantic search capabilities for codebases and documents. The API is organized into several key packages:

- `com.nurujjamanpollob.textenginejava.rag`: Main package containing core classes
- `com.nurujjamanpollob.textenginejava.rag.config`: Configuration management
- `com.nurujjamanpollob.textenginejava.rag.context`: Semantic search and embedding
- `com.nurujjamanpollob.textenginejava.rag.exception`: Custom exceptions
- `com.nurujjamanpollob.textenginejava.rag.io`: File I/O and monitoring
- `com.nurujjamanpollob.textenginejava.rag.model`: Data models
- `com.nurujjamanpollob.textenginejava.rag.utils`: Utility classes

## Core Classes

### Main

The entry point for the TextGraph RAG Java application.

**Package**: `com.nurujjamanpollob.textenginejava.rag`

**Description**: This class provides the command-line interface and main application loop.

**Usage**:
```java
public class Main {
    public static void main(String[] args) {
        // Application entry point
    }
}
```

### ProjectOrchestrator

Manages the lifecycle of projects including loading, indexing, and file monitoring.

**Package**: `com.nurujjamanpollob.textenginejava.rag`

**Constructor**:
```java
public ProjectOrchestrator(ContextCollector collector)
```

**Key Methods**:

#### loadAndSyncProject
```java
public void loadAndSyncProject(String projectId, String rootDirPath)
```
Loads and synchronizes a project with the file system.

**Parameters**:
- `projectId`: Unique identifier for the project
- `rootDirPath`: Path to the project root directory

**Description**: 
This method loads a project, analyzes its structure, and indexes all valid files. It also starts file watching for real-time updates.

#### searchProject
```java
public void searchProject(String projectId, String query)
public void searchProject(String projectId, String query, int maxResults, double minScore)
```
Performs semantic search on a project.

**Parameters**:
- `projectId`: Unique identifier for the project
- `query`: Search query string
- `maxResults`: Maximum number of results to return (optional)
- `minScore`: Minimum similarity score threshold (optional)

**Description**: 
Converts the query to a semantic embedding and searches the index for similar content.

#### createRagMetadata
```java
public void createRagMetadata(String projectId, String rootDirPath) throws Exception
```
Creates RAG metadata for a project.

**Parameters**:
- `projectId`: Unique identifier for the project
- `rootDirPath`: Path to the project root directory

**Description**: 
Initializes the metadata files required for RAG functionality.

#### handleFileChange
```java
public void handleFileChange(String projectId, Path filePath, String eventType)
```
Handles file system events and updates the index accordingly.

**Parameters**:
- `projectId`: Unique identifier for the project
- `filePath`: Path to the changed file
- `eventType`: Type of file event ("MODIFY" or "DELETE")

### ContextCollector

Manages vector embeddings and semantic search capabilities.

**Package**: `com.nurujjamanpollob.textenginejava.rag.context`

**Constructor**:
```java
public ContextCollector()
```

**Key Methods**:

#### updateFile
```java
public void updateFile(String projectId, String relativePath, String content)
```
Indexes a file's content using semantic embeddings.

**Parameters**:
- `projectId`: Unique identifier for the project
- `relativePath`: Relative path to the file within the project
- `content`: Content of the file to index

#### updateFileStreaming
```java
public void updateFileStreaming(String projectId, String relativePath, Path filePath)
```
Indexes a file using streaming for memory efficiency with large files.

**Parameters**:
- `projectId`: Unique identifier for the project
- `relativePath`: Relative path to the file within the project
- `filePath`: Path to the file to index

#### searchGrouped
```java
public Map<String, List<String>> searchGrouped(String query, String projectId, int maxResults, double minScore)
```
Performs semantic search and groups results by file.

**Parameters**:
- `query`: Search query string
- `projectId`: Unique identifier for the project
- `maxResults`: Maximum number of results to return
- `minScore`: Minimum similarity score threshold

**Returns**: Map of file paths to lists of relevant text segments

#### saveIndexToDisk
```java
public void saveIndexToDisk(String projectId, Path projectRoot)
```
Saves embeddings to disk for persistence.

**Parameters**:
- `projectId`: Unique identifier for the project
- `projectRoot`: Path to the project root directory

#### loadIndexFromDisk
```java
public void loadIndexFromDisk(String projectId, Path projectRoot)
```
Loads embeddings from disk.

**Parameters**:
- `projectId`: Unique identifier for the project
- `projectRoot`: Path to the project root directory

### FileWatcher

Monitors file system changes recursively and notifies the orchestrator.

**Package**: `com.nurujjamanpollob.textenginejava.rag.io`

**Constructor**:
```java
public FileWatcher(Path rootDir, ProjectOrchestrator orchestrator, String projectId)
```

**Key Methods**:

#### run
```java
@Override
public void run()
```
Starts the file watching process.

**Description**: 
Monitors all subdirectories for file changes and notifies the orchestrator.

## Configuration Classes

### RagConfig

Manages external configuration parameters.

**Package**: `com.nurujjamanpollob.textenginejava.rag.config`

**Singleton Access**:
```java
public static synchronized RagConfig getInstance()
```

**Key Methods**:

#### getChunkSize
```java
public int getChunkSize()
```
Returns the document chunk size for embedding.

**Default**: 1000

#### getOverlapSize
```java
public int getOverlapSize()
```
Returns the overlap between document chunks.

**Default**: 150

#### getMaxFileSize
```java
public int getMaxFileSize()
```
Returns the maximum file size for in-memory processing.

**Default**: 10485760 (10MB)

#### getMinScore
```java
public double getMinScore()
```
Returns the minimum similarity score for search results.

**Default**: 0.6

#### getMaxSearchResults
```java
public int getMaxSearchResults()
```
Returns the maximum number of search results to return.

**Default**: 15

## Model Classes

### ProjectMetadata

Stores metadata about a project for efficient synchronization.

**Package**: `com.nurujjamanpollob.textenginejava.rag.model`

**Fields**:
- `fileHashes`: Map of relative file paths to SHA-256 hashes
- `lastIndexed`: Timestamp of last indexing operation

**Key Methods**:

#### getFileHashes
```java
public Map<String, String> getFileHashes()
```
Returns the map of file hashes.

#### setFileHashes
```java
public void setFileHashes(Map<String, String> fileHashes)
```
Sets the map of file hashes.

#### getLastIndexed
```java
public long getLastIndexed()
```
Returns the timestamp of last indexing operation.

#### setLastIndexed
```java
public void setLastIndexed(long lastIndexed)
```
Sets the timestamp of last indexing operation.

### ProjectProfile

Holds statistical information about a project.

**Package**: `com.nurujjamanpollob.textenginejava.rag.model`

**Constructor**:
```java
public ProjectProfile(String projectName, String rootPath)
```

**Key Methods**:

#### addFile
```java
public void addFile(String extension, long size)
```
Adds a file to the project profile.

#### getFormattedSize
```java
public String getFormattedSize()
```
Returns the formatted total size of all files.

#### toString
```java
@Override
public String toString()
```
Returns a string representation of the project profile.

### RAGSearchResult

Represents a single search result.

**Package**: `com.nurujjamanpollob.textenginejava.rag.model`

**Constructor**:
```java
public RAGSearchResult(String content, double score, String filePath)
```

**Key Methods**:

#### getContent
```java
public String getContent()
```
Returns the relevant text content.

#### getScore
```java
public double getScore()
```
Returns the similarity score (0.0 to 1.0).

#### getFilePath
```java
public String getFilePath()
```
Returns the path to the source file.

## Utility Classes

### PathValidator

Provides secure path validation to prevent directory traversal attacks.

**Package**: `com.nurujjamanpollob.textenginejava.rag.utils`

**Key Methods**:

#### validateAndNormalizePath
```java
public static Path validateAndNormalizePath(String inputPath, Path basePath) throws RagException
```
Validates and normalizes a path, ensuring it's within the allowed base path.

#### validatePath
```java
public static Path validatePath(String inputPath) throws RagException
```
Validates a path for suspicious patterns like ".." or "~".

### RagLogger

Provides standardized logging with colored output.

**Package**: `com.nurujjamanpollob.textenginejava.rag.utils`

**Key Methods**:

#### info
```java
public static void info(String message)
```
Logs an informational message.

#### warn
```java
public static void warn(String message)
```
Logs a warning message.

#### error
```java
public static void error(String message)
```
Logs an error message.

#### debug
```java
public static void debug(String message)
```
Logs a debug message.

### HashUtils

Calculates file hashes efficiently using streaming.

**Package**: `com.nurujjamanpollob.textenginejava.rag.utils`

**Key Methods**:

#### calculateFileHash
```java
public static String calculateFileHash(Path path)
```
Calculates the SHA-256 hash of a file.

### ProjectInfoExtractor

Analyzes project structure and provides statistics.

**Package**: `com.nurujjamanpollob.textenginejava.rag.utils`

**Key Methods**:

#### analyzeProject
```java
public static ProjectProfile analyzeProject(String projectId, Path rootDir)
```
Scans the directory and returns a statistical profile.

## Exception Classes

### RagException

Custom exception class for RAG-specific errors.

**Package**: `com.nurujjamanpollob.textenginejava.rag.exception`

**Constructors**:
```java
public RagException(String message)
public RagException(String message, Throwable cause)
```

## Usage Examples

### Basic Project Loading and Search

```java
import com.nurujjamanpollob.textenginejava.rag.context.ContextCollector;
import com.nurujjamanpollob.textenginejava.rag.ProjectOrchestrator;

public class Example {
    public static void main(String[] args) {
        // Initialize components
        ContextCollector collector = new ContextCollector();
        ProjectOrchestrator orchestrator = new ProjectOrchestrator(collector);
        
        // Load a project
        String projectId = "my-project";
        String projectPath = "/path/to/project";
        
        try {
            orchestrator.loadAndSyncProject(projectId, projectPath);
            
            // Perform a search
            orchestrator.searchProject(projectId, "How does file watching work?");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Custom Configuration

```java
import com.nurujjamanpollob.textenginejava.rag.config.RagConfig;

public class ConfigExample {
    public static void main(String[] args) {
        RagConfig config = RagConfig.getInstance();
        
        // Get configuration values
        int chunkSize = config.getChunkSize();
        int maxFileSize = config.getMaxFileSize();
        double minScore = config.getMinScore();
        
        System.out.println("Chunk size: " + chunkSize);
        System.out.println("Max file size: " + maxFileSize);
        System.out.println("Min score: " + minScore);
    }
}
```

### File Hash Calculation

```java
import com.nurujjamanpollob.textenginejava.rag.utils.HashUtils;
import java.nio.file.Paths;

public class HashExample {
    public static void main(String[] args) {
        try {
            String hash = HashUtils.calculateFileHash(Paths.get("/path/to/file.txt"));
            System.out.println("File hash: " + hash);
        } catch (Exception e) {
            System.err.println("Error calculating hash: " + e.getMessage());
        }
    }
}
```

### Path Validation

```java
import com.nurujjamanpollob.textenginejava.rag.utils.PathValidator;
import com.nurujjamanpollob.textenginejava.rag.exception.RagException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidationExample {
    public static void main(String[] args) {
        try {
            Path basePath = Paths.get("/safe/directory");
            Path validatedPath = PathValidator.validateAndNormalizePath("../etc/passwd", basePath);
            System.out.println("Validated path: " + validatedPath);
        } catch (RagException e) {
            System.err.println("Invalid path: " + e.getMessage());
        }
    }
}
```

## Integration with Other Systems

### Using as a Library

The TextGraph RAG Java system can be integrated into other Java applications as a library:

1. Add the JAR to your classpath
2. Import the necessary classes
3. Use the API as shown in the examples above

### Extending Functionality

To extend the system:

1. Create new classes that implement desired functionality
2. Use the existing utility classes for consistency
3. Follow the established patterns for error handling and logging
4. Update the configuration system if new parameters are needed

## Best Practices

### Error Handling

Always handle `RagException` and other exceptions appropriately:

```java
try {
    orchestrator.loadAndSyncProject(projectId, projectPath);
} catch (RagException e) {
    RagLogger.error("Project loading failed: " + e.getMessage());
    // Handle the error appropriately
} catch (Exception e) {
    RagLogger.error("Unexpected error: " + e.getMessage());
    // Handle unexpected errors
}
```

### Resource Management

Ensure proper cleanup of resources:

```java
// The system handles most resource management automatically
// but be aware of thread management for file watchers
```

### Configuration

Use the configuration system for all tunable parameters:

```java
RagConfig config = RagConfig.getInstance();
int chunkSize = config.getChunkSize(); // Instead of hardcoded values
```

## Version Compatibility

This API documentation corresponds to version 1.0.0 of the TextGraph RAG Java system. Future versions may introduce additional features while maintaining backward compatibility for core functionality.

## Support

For issues with the API or documentation, please:

1. Check the [GitHub Issues](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/issues) page
2. Review the [Technical Documentation](TECHNICAL_DOCUMENTATION.md)
3. Contact the maintainers through GitHub