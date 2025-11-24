# Changelog

All notable changes to the TextGraph RAG Java project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Initial Hotfix Release - Nov 25, 2025

### Added
- Path validation utility to prevent directory traversal attacks
- Secure exception handling that doesn't expose sensitive information
- Streaming processing for large files to prevent memory issues
- Recursive file watching to monitor all subdirectories
- External configuration management using properties files
- Standardized logging utility for consistent logging
- Custom exception classes for better error handling
- Comprehensive README.md documentation
- CHANGELOG.md to track changes

### Changed
- Improved input validation for all user-provided paths
- Enhanced error handling with unified approach
- Updated file processing to handle large files efficiently
- Refactored file watching to recursively monitor directories
- Externalized configuration parameters
- Improved code documentation with detailed Javadoc comments
- Enhanced security measures for file path handling

### Fixed
- Path traversal vulnerability in ProjectOrchestrator
- Inefficient file watching that only monitored root directory
- Memory inefficiency in large file processing
- Inconsistent error handling across components
- Hardcoded values replaced with configurable parameters

### Security
- Implemented path validation and normalization to prevent directory traversal
- Improved exception handling to prevent information disclosure
- Added input validation for all user-provided data

## Initial Release - Nov 24, 2025

### Added
- Initial release of the TextGraph RAG Java system
- Basic RAG functionality for semantic code search
- File watching capabilities
- Document processing and embedding
- Simple command-line interface
