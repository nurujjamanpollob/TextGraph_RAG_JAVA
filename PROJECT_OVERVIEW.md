# TextGraph RAG Java Project Overview

Welcome to the TextGraph RAG Java project - a sophisticated semantic search system for codebases and documents.

## Project Status

**Version**: 1.0.0 (Enhanced)
**Status**: Production Ready
**Last Updated**: April 2025

## Project Description

TextGraph RAG Java is an advanced Retrieval-Augmented Generation (RAG) system that enables deep semantic analysis of unstructured text. Unlike traditional keyword search tools, it uses vector embeddings to understand context and meaning, allowing users to interact with local documents through natural language queries.

The system is particularly effective for:
- Code search and analysis
- Technical documentation exploration
- Knowledge base querying
- Project onboarding and understanding

## Key Features

### Semantic Search
- Context-aware document understanding
- Natural language query processing
- Relevance-ranked results

### Intelligent Indexing
- Automatic project analysis
- Smart delta updates
- Large file streaming processing

### Real-time Monitoring
- Recursive file watching
- Automatic index updates
- Change detection and processing

### Security Enhanced
- Path traversal prevention
- Secure exception handling
- Input validation and sanitization

### Performance Optimized
- Memory-efficient processing
- Configurable parameters
- Smart caching and persistence

## Technology Stack

- **Language**: Java 11+
- **Framework**: LangChain4j
- **Embedding Model**: All-MiniLM-L6-v2
- **Build Tool**: Gradle
- **Dependencies**: Gson, TextFileDetector

## Documentation Map

### Getting Started
- [Quick Start Guide](QUICK_START.md) - Fastest path to usage
- [Installation Guide](INSTALLATION_GUIDE.md) - Detailed setup instructions
- [User Guide](USER_GUIDE.md) - Comprehensive usage documentation

### Technical Information
- [README](README.md) - Project overview and quick reference
- [Technical Documentation](TECHNICAL_DOCUMENTATION.md) - In-depth technical details
- [API Documentation](API_DOCUMENTATION.md) - Public API reference

### Project Information
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md) - Enhancement overview
- [Final Implementation Report](FINAL_IMPLEMENTATION_REPORT.md) - Detailed implementation
- [Changelog](CHANGELOG.md) - Version history
- [Contributing Guide](CONTRIBUTING.md) - How to contribute
- [License](LICENSE.txt) - MIT License

## System Architecture

The system follows a modular architecture with clear separation of concerns:

```
┌─────────────────┐
│   CLI Interface │
└─────────┬───────┘
          │
┌─────────▼────────┐
│ Project Manager  │
└─────────┬────────┘
          │
┌─────────▼────────┐    ┌─────────────────┐
│  Index Manager   │◄──►│  File Watcher   │
└─────────┬────────┘    └─────────────────┘
          │
┌─────────▼────────┐
│ Vector Embedding │
│    Processor     │
└─────────┬────────┘
          │
┌─────────▼────────┐
│  Data Storage    │
└──────────────────┘
```

## Security Features

The enhanced version includes robust security measures:

- **Path Validation**: Prevents directory traversal attacks
- **Exception Sanitization**: Protects against information disclosure
- **Input Validation**: Ensures all inputs are safe
- **Secure Configuration**: Externalized sensitive settings

## Performance Features

Optimized for efficiency and scalability:

- **Streaming Processing**: Handles large files without memory issues
- **Delta Updates**: Only processes changed files
- **Recursive Watching**: Monitors all subdirectories
- **Configurable Limits**: Adjustable performance parameters

## Recent Enhancements

Based on a comprehensive analysis, the system has been enhanced with:

### Security Improvements
- Path traversal vulnerability fixes
- Secure exception handling
- Input validation for all user inputs

### Performance Optimizations
- Recursive file watching implementation
- Memory-efficient large file processing
- Smart synchronization with delta updates

### Code Quality Enhancements
- External configuration management
- Standardized logging
- Improved documentation

### Maintainability Improvements
- Modular design with utility classes
- Better separation of concerns
- Comprehensive test coverage

## Usage Scenarios

### Codebase Exploration
```
load /path/to/java-project
search Show me all classes that implement Runnable
```

### Documentation Search
```
load /path/to/docs
search How do I configure the database connection?
```

### Knowledge Base Querying
```
load /path/to/kb
search What are the troubleshooting steps for timeout errors?
```

## Getting Help

For support with the TextGraph RAG Java system:

1. **Documentation**: Start with the relevant documentation files
2. **Issues**: Check the [GitHub Issues](https://github.com/nurujjamanpollob/TextGraph_RAG_JAVA/issues) page
3. **Community**: Engage with other users and contributors
4. **Maintainers**: Contact the project maintainers directly

## Contributing

Contributions are welcome and appreciated:

1. **Bug Reports**: Submit detailed issue reports
2. **Feature Requests**: Propose new functionality
3. **Code Contributions**: Submit pull requests with improvements
4. **Documentation**: Help improve the documentation

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.

## License

This project is licensed under the MIT License - see [LICENSE.txt](LICENSE.txt) for details.

## Acknowledgments

This project builds upon and integrates with several excellent open-source technologies:

- [LangChain4j](https://github.com/langchain4j/langchain4j) for Java-based LLM integration
- [Gson](https://github.com/google/gson) for JSON serialization
- [All-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) for embedding model

Special thanks to all contributors and the open-source community for making this project possible.