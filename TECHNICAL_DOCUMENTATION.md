# TextGraph RAG Java Technical Documentation

This document provides detailed technical information about the TextGraph RAG Java system, including architecture, implementation details, and design decisions.

## Table of Contents

- [System Architecture](#system-architecture)
- [Core Components](#core-components)
- [Data Models](#data-models)
- [Utility Classes](#utility-classes)
- [Security Implementation](#security-implementation)
- [Performance Optimizations](#performance-optimizations)
- [Configuration Management](#configuration-management)
- [Error Handling](#error-handling)
- [Persistence Layer](#persistence-layer)
- [Dependencies](#dependencies)
- [Build Process](#build-process)
- [Testing](#testing)

## System Architecture

The TextGraph RAG Java system follows a modular architecture designed for maintainability, security, and performance:

```
┌─────────────────────────────────────────────────────────────┐
│                      Main Interface                         │
│                   (Command Line UI)                         │
├─────────────────────────────────────────────────────────────┤
│                    ProjectOrchestrator                      │
│            (Project lifecycle management)                   │
├─────────────────────────────────────────────────────────────┤
│  ContextCollector  │  FileWatcher  │  ProjectInfoExtractor  │
│  (Semantic search) │ (File monitor)│   (Project analysis)   │
├─────────────────────────────────────────────────────────────┤
│        Utilities        │         Models         │          │
│  (PathValidator, etc.)  │ (ProjectMetadata, etc.)│          │
├─────────────────────────────────────────────────────────────┤
│                   External Dependencies                     │
│         (LangChain4j, Gson, TextFileDetector)              │
└─────────────────────────────────────────────────────────────┘
```

### Key Design Principles

1. **Separation of Concerns**: Each component has a single responsibility
2. **Modularity**: Components are loosely coupled and easily testable
3. **Security First**: All user inputs are validated and sanitized
4. **Performance Optimized**: Efficient algorithms and data structures
5. **Configurable**: Behavior can be adjusted without code changes
6. **Maintainable**: Clear code structure with comprehensive documentation

## Core Components

### Main Class

The `Main` class serves as the entry point and provides the command-line interface.

**Key Responsibilities:**
- Parse user commands
- Manage the current project context
- Coordinate between components
- Handle user interaction

**Implementation Details:**
- Uses a simple REPL (Read-Eval-Print Loop) pattern
- Maintains state for the currently loaded project
- Provides basic command validation
- Handles graceful shutdown

### ProjectOrchestrator

The `ProjectOrchestrator` manages the lifecycle of projects and coordinates between components.

**Key Responsibilities:**
- Project loading and initialization
- Metadata management
- Index synchronization
- File change handling
- Search coordination

**Implementation Details:**
- Maintains maps of active projects and their metadata
- Implements smart synchronization using file hashes
- Manages file watchers for each project
- Handles file validation and filtering

**Key Methods:**

```java
public void loadAndSyncProject(String projectId, String rootDirPath)
```
Loads a project and synchronizes its index with the file system.

```java
public void handleFileChange(String projectId, Path filePath, String eventType)
```
Handles file system events and updates the index accordingly.

```java
public void searchProject(String projectId, String query, int maxResults, double minScore)
```
Performs semantic search on a project with configurable parameters.

### ContextCollector

The `ContextCollector` handles document processing, embedding, and semantic search.

**Key Responsibilities:**
- Document chunking and embedding
- Vector storage management
- Semantic search implementation
- Index persistence

**Implementation Details:**
- Uses LangChain4j for embedding generation
- Implements streaming for large file processing
- Maintains separate embedding stores per project
- Handles document metadata management

**Key Methods:**

```java
public void updateFileStreaming(String projectId, String relativePath, Path filePath)
```
Processes files using streaming to handle large files efficiently.

```java
public Map<String, List<String>> searchGrouped(String query, String projectId, int maxResults, double minScore)
```
Performs semantic search and groups results by file.

## Data Models

### ProjectMetadata

Stores metadata about a project for efficient synchronization.

**Fields:**
- `fileHashes`: Map of relative file paths to SHA-256 hashes
- `lastIndexed`: Timestamp of last indexing operation

**Purpose:**
- Enable delta updates by comparing file hashes
- Track when the project was last indexed
- Maintain consistency between file system and index

### ProjectProfile

Holds statistical information about a project.

**Fields:**
- `projectName`: Name of the project
- `rootPath`: Absolute path to project root
- `totalFiles`: Total number of files in the project
- `totalSizeBytes`: Total size of all files
- `languageBreakdown`: Map of file extensions to counts

**Purpose:**
- Provide project statistics during loading
- Help users understand project composition
- Enable filtering of irrelevant files

### RAGSearchResult

Represents a single search result.

**Fields:**
- `content`: The relevant text content
- `score`: Similarity score (0.0 to 1.0)
- `filePath`: Path to the source file

**Purpose:**
- Standardize search result representation
- Enable sorting by relevance score
- Provide context for search results

## Utility Classes

### PathValidator

Provides secure path validation to prevent directory traversal attacks.

**Key Methods:**

```java
public static Path validateAndNormalizePath(String inputPath, Path basePath)
```
Validates and normalizes a path, ensuring it's within the allowed base path.

```java
public static Path validatePath(String inputPath)
```
Validates a path for suspicious patterns like ".." or "~".

**Security Features:**
- Path normalization to prevent traversal
- Suspicious pattern detection
- Base path restriction
- Exception sanitization

### RagConfig

Manages external configuration parameters.

**Key Features:**
- Singleton pattern for global access
- Properties file loading
- Default value fallback
- Type-safe parameter access

**Configuration Parameters:**
- `chunk.size`: Document chunk size for embedding
- `overlap.size`: Overlap between chunks
- `max.file.size`: Maximum file size for in-memory processing
- `min.score`: Minimum similarity score for search results
- `max.search.results`: Maximum number of search results

### RagLogger

Provides standardized logging with colored output.

**Key Features:**
- Timestamped log messages
- Colored output for different log levels
- Consistent formatting
- Security-conscious message handling

**Log Levels:**
- `info`: General operational messages
- `warn`: Potential issues
- `error`: Problems affecting functionality
- `debug`: Detailed troubleshooting information

### HashUtils

Calculates file hashes efficiently using streaming.

**Key Features:**
- SHA-256 hash calculation
- Streaming processing for large files
- Memory-efficient implementation
- Error handling with sanitization

### ProjectInfoExtractor

Analyzes project structure and provides statistics.

**Key Features:**
- File system traversal with filtering
- Language detection by extension
- Size calculation
- Ignored file type filtering

## Security Implementation

### Path Traversal Prevention

The system implements multiple layers of protection against directory traversal:

1. **Input Validation**: All paths are checked for suspicious patterns
2. **Path Normalization**: Paths are normalized to prevent traversal sequences
3. **Base Path Restriction**: Resolved paths must be within the project directory
4. **Exception Sanitization**: Error messages don't expose system information

### Secure Exception Handling

Custom exception handling prevents information disclosure:

1. **RagException**: Custom exception class for RAG-specific errors
2. **Message Sanitization**: Sensitive information is removed from error messages
3. **Logging Separation**: Detailed errors are logged, generic messages returned to users

### Input Validation

All user inputs undergo validation:

1. **Path Validation**: Using `PathValidator` utility
2. **Command Validation**: Checking command syntax and parameters
3. **Size Limits**: Preventing processing of excessively large inputs

## Performance Optimizations

### Recursive File Watching

The `FileWatcher` implementation monitors all subdirectories:

1. **Recursive Registration**: Registers watch keys for all subdirectories
2. **Dynamic Updates**: Automatically registers new directories
3. **Efficient Event Handling**: Processes events with debouncing

### Memory-Efficient Large File Processing

Streaming processing prevents memory issues with large files:

1. **Size Checking**: Files are checked against configured limits
2. **Chunked Processing**: Large files are processed in chunks
3. **Buffer Management**: Efficient buffer usage during processing

### Smart Synchronization

Delta updates reduce unnecessary processing:

1. **Hash Comparison**: Files are only reprocessed if they've changed
2. **Selective Updates**: Only modified files trigger re-indexing
3. **Deletion Handling**: Removed files are cleaned from the index

### Efficient Data Structures

The system uses appropriate data structures for performance:

1. **HashMaps**: For fast metadata lookups
2. **Streaming**: For large file processing
3. **In-Memory Vector Store**: For fast semantic search

## Configuration Management

### External Configuration

All configurable parameters are externalized:

1. **Properties File**: `rag-config.properties` in resources
2. **Singleton Access**: `RagConfig` class provides global access
3. **Default Values**: Safe defaults for all parameters
4. **Type Safety**: Automatic type conversion with validation

### Configuration Parameters

| Parameter | Type | Default | Purpose |
|-----------|------|---------|---------|
| chunk.size | int | 1000 | Document chunk size |
| overlap.size | int | 150 | Chunk overlap |
| max.file.size | int | 10485760 | Max file size (10MB) |
| min.score | double | 0.6 | Min search similarity |
| max.search.results | int | 15 | Max search results |

### Dynamic Configuration

Configuration changes require application restart but provide:

1. **Flexibility**: Adjust behavior without code changes
2. **Environment Adaptation**: Different settings for different environments
3. **Performance Tuning**: Optimize for specific use cases

## Error Handling

### Custom Exception Classes

The system uses custom exceptions for better error categorization:

```java
public class RagException extends Exception {
    public RagException(String message) {
        super(message);
    }
    
    public RagException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Standardized Logging

All components use `RagLogger` for consistent error reporting:

```java
try {
    // Some operation
} catch (Exception e) {
    RagLogger.error("Failed to process file: " + e.getMessage());
    throw new RagException("Processing failed", e);
}
```

### Graceful Degradation

The system handles errors gracefully:

1. **Partial Failures**: One file error doesn't stop the entire process
2. **Recovery Mechanisms**: Corrupted indexes can be rebuilt
3. **User Feedback**: Clear error messages guide users to solutions

## Persistence Layer

### Index Storage

Indexes are stored in JSON format for portability:

1. **Embedding Storage**: Vector embeddings saved as JSON
2. **Metadata Storage**: Project metadata in structured format
3. **File-Based**: No external database required
4. **Version Compatible**: JSON format ensures compatibility

### Data Serialization

LangChain4j's built-in serialization is used:

```java
// Saving
String json = store.serializeToJson();
Files.writeString(indexFile, json);

// Loading
InMemoryEmbeddingStore<TextSegment> store = InMemoryEmbeddingStore.fromJson(json);
```

### Persistence Strategy

1. **Per-Project Storage**: Each project has its own `.rag_data` directory
2. **Automatic Saving**: Indexes saved after updates
3. **Lazy Loading**: Indexes loaded only when needed
4. **Atomic Operations**: File operations are atomic to prevent corruption

## Dependencies

### Core Dependencies

1. **LangChain4j**: For embedding generation and vector storage
2. **Gson**: For JSON serialization
3. **TextFileDetector**: For file type detection

### LangChain4j Components

```gradle
implementation("dev.langchain4j:langchain4j:1.8.0")
implementation("dev.langchain4j:langchain4j-open-ai:1.8.0")
implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.8.0-beta15")
```

### Testing Dependencies

```gradle
testImplementation platform('org.junit:junit-bom:5.10.0')
testImplementation 'org.junit.jupiter:junit-jupiter'
```

## Build Process

### Gradle Build File

The `build.gradle` file defines the build process:

```gradle
plugins {
    id 'java'
}

group = 'com.nurujjamanpollob.textenginejava.rag'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    // LangChain4j dependencies
    implementation("dev.langchain4j:langchain4j:1.8.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.8.0")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.8.0-beta15")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.13.2")
    
    // Local JARs
    implementation fileTree(dir: 'JAR-LIBS', include: ['*.jar'])
    
    // Testing
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}
```

### Build Commands

1. **Build**: `./gradlew build`
2. **Run**: `./gradlew run`
3. **Create JAR**: `./gradlew jar`
4. **Test**: `./gradlew test`

## Testing

### Unit Tests

Simple unit tests verify core functionality:

```java
@Test
public void testConfigurationDefaults() {
    RagConfig config = RagConfig.getInstance();
    assertTrue(config.getChunkSize() > 0, "Chunk size should be positive");
    assertTrue(config.getMaxFileSize() > 0, "Max file size should be positive");
}

@Test
public void testPathValidation_Traversal() {
    Exception exception = assertThrows(Exception.class, () -> {
        PathValidator.validatePath("../../../etc/passwd");
    });
    assertTrue(exception.getMessage().contains("Invalid") || 
               exception.getMessage().contains("suspicious"));
}
```

### Test Coverage

Current tests cover:
1. **Configuration Loading**: Verifying default values
2. **Path Validation**: Testing security features
3. **Basic Functionality**: Core component interactions

### Future Testing Improvements

Planned testing enhancements:
1. **Integration Tests**: End-to-end workflow testing
2. **Performance Tests**: Large project processing benchmarks
3. **Security Tests**: Comprehensive vulnerability testing
4. **Regression Tests**: Ensuring backward compatibility

## Implementation Summary

### Security Enhancements

1. **Path Validation**: Prevents directory traversal attacks
2. **Exception Sanitization**: Protects against information disclosure
3. **Input Validation**: Ensures all inputs are safe

### Performance Improvements

1. **Recursive Watching**: Monitors all subdirectories
2. **Streaming Processing**: Handles large files efficiently
3. **Delta Updates**: Only processes changed files

### Code Quality Improvements

1. **External Configuration**: All parameters configurable
2. **Standardized Logging**: Consistent error reporting
3. **Modular Design**: Clear separation of concerns

### Maintainability Enhancements

1. **Utility Classes**: Reusable helper functions
2. **Documentation**: Comprehensive Javadoc comments
3. **Configuration Management**: Easy parameter adjustment

## Conclusion

The TextGraph RAG Java system represents a significant improvement over the original implementation, with enhanced security, performance, and maintainability. The modular architecture, comprehensive documentation, and robust error handling make it suitable for production use while providing a solid foundation for future enhancements.