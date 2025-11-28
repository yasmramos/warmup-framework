# Contributing to Warmup Framework

We love your input! We want to make contributing to the Warmup Framework as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

## Pull Requests

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. Fork the repo and create your branch from `master`
2. If you've added code that should be tested, add tests
3. If you've changed APIs, update the documentation
4. Ensure the test suite passes
5. Make sure your code lints
6. Issue that pull request!

### Pull Request Guidelines

1. **Code Style**: Follow the existing code style. We use standard Java conventions.
2. **Tests**: Add tests for any new functionality
3. **Documentation**: Update documentation for any API changes
4. **Performance**: Ensure no performance regressions
5. **Backward Compatibility**: Maintain backward compatibility when possible

## Coding Standards

### Code Style
- Use 4 spaces for indentation
- Follow Java naming conventions
- Maximum line length: 120 characters
- Use meaningful variable and method names

### Example Code Style
```java
@Singleton
public class ExampleService {
    
    private final DependencyRegistry dependencyRegistry;
    private final PerformanceMetrics performanceMetrics;
    
    public ExampleService(DependencyRegistry dependencyRegistry,
                         PerformanceMetrics performanceMetrics) {
        this.dependencyRegistry = Objects.requireNonNull(dependencyRegistry);
        this.performanceMetrics = Objects.requireNonNull(performanceMetrics);
    }
    
    public Result process(Request request) {
        Objects.requireNonNull(request, "Request cannot be null");
        
        performanceMetrics.startTimer("process.operation");
        try {
            return executeProcess(request);
        } finally {
            performanceMetrics.stopTimer("process.operation");
        }
    }
}
```

### Documentation Comments
```java
/**
 * Processes requests using advanced dependency injection and performance monitoring.
 * 
 * This method implements a highly optimized processing pipeline with O(1) complexity
 * for dependency resolution and built-in performance metrics collection.
 *
 * @param request the request to process
 * @return the processed result
 * @throws IllegalArgumentException if request is null
 * @since 1.0.0
 */
public Result process(Request request) {
    // Implementation
}
```

## Testing Guidelines

### Unit Tests
```java
@ExtendWith(WarmupTestExtension.class)
class ExampleServiceTest {
    
    @Inject
    private ExampleService service;
    
    @Test
    @DisplayName("Should process valid requests successfully")
    void shouldProcessValidRequests() {
        // Given
        Request request = createValidRequest();
        
        // When
        Result result = service.process(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw exception for null requests")
    void shouldThrowExceptionForNullRequests() {
        // Given
        Request nullRequest = null;
        
        // When & Then
        assertThatThrownBy(() -> service.process(nullRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Request cannot be null");
    }
}
```

### Integration Tests
```java
@SpringBootTest
class ContainerIntegrationTest {
    
    @Autowired
    private WarmupContainer container;
    
    @Test
    @DisplayName("Should properly initialize container with all beans")
    void shouldInitializeContainerWithAllBeans() throws Exception {
        // Given
        container.start();
        
        // When
        ExampleService service = container.get(ExampleService.class);
        
        // Then
        assertThat(service).isNotNull();
        assertThat(service.isInitialized()).isTrue();
    }
}
```

## Performance Testing

### JMH Benchmark Template
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class ServiceBenchmark {
    
    private ExampleService service;
    private Request request;
    
    @Setup
    public void setup() {
        service = new ExampleService(/* dependencies */);
        request = createTestRequest();
    }
    
    @Benchmark
    public Result processRequest() {
        return service.process(request);
    }
    
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(ServiceBenchmark.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}
```

## Reporting Bugs

We use GitHub issues to track public bugs. Report a bug by opening a new issue.

### Bug Report Template
```markdown
**Bug Description**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. See error

**Expected Behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment:**
- Java Version: [e.g. 8, 11, 17]
- OS: [e.g. macOS, Windows, Linux]
- Framework Version: [e.g. 1.0.0]

**Additional Context**
Add any other context about the problem here.
```

## Feature Requests

We welcome feature requests! Please provide as much detail and context as possible.

### Feature Request Template
```markdown
**Feature Description**
A clear and concise description of the feature you'd like to see implemented.

**Problem it Solves**
Describe the problem this feature would solve.

**Proposed Solution**
Describe how you envision this feature working.

**Alternatives Considered**
Describe any alternative solutions or features you've considered.

**Additional Context**
Add any other context, mockups, or examples about the feature request here.
```

## Performance Guidelines

### Performance Requirements
- Container startup should remain under **1ms** for basic configurations
- Bean resolution must maintain **O(1)** complexity
- Memory usage should not increase by more than **10%** per feature addition
- All performance-sensitive code must include benchmarks

### Performance Testing Checklist
- [ ] Run existing benchmarks to ensure no regressions
- [ ] Add new benchmarks for new performance-sensitive features
- [ ] Test with different container configurations
- [ ] Verify memory usage impact
- [ ] Document performance characteristics

## Security Guidelines

### Security Best Practices
- Never commit sensitive data (API keys, passwords, etc.)
- Use parameterized queries to prevent injection attacks
- Validate all input parameters
- Follow the principle of least privilege
- Report security vulnerabilities privately

### Security Review Checklist
- [ ] All user inputs are validated
- [ ] No sensitive data in logs or error messages
- [ ] Proper access control implemented
- [ ] Dependencies are up-to-date and secure
- [ ] Code follows OWASP guidelines

## Documentation Guidelines

### API Documentation
- Document all public methods with JavaDoc
- Include examples for complex APIs
- Document performance characteristics
- Include thread-safety information
- Specify requirements and constraints

### User Documentation
- Update README.md for user-facing changes
- Add examples for new features
- Update changelog for all changes
- Include migration guides for breaking changes

## Build and Deployment

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher
- Git

### Development Setup
```bash
# Clone the repository
git clone https://github.com/your-org/warmup-framework.git
cd warmup-framework

# Build the project
mvn clean install

# Run tests
mvn test

# Run integration tests
mvn integration-test

# Run performance benchmarks
mvn test -Pbenchmark
```

### Continuous Integration
We use GitHub Actions for CI/CD:
- All pull requests must pass CI checks
- Tests are run on multiple Java versions
- Performance benchmarks are compared against baselines
- Code coverage must not decrease

## Code Review Process

All submissions require review. We use GitHub pull requests for this purpose.

### Review Checklist
- [ ] Code follows style guidelines
- [ ] All tests pass
- [ ] Performance impact assessed
- [ ] Documentation updated
- [ ] Security considerations addressed
- [ ] Backward compatibility maintained

## Community Guidelines

### Be Respectful
- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Give and gracefully accept constructive feedback
- Focus on what is best for the community

### Code of Conduct
This project adheres to the Contributor Covenant Code of Conduct. By participating, you are expected to uphold this code.

## Release Process

### Version Numbering
We follow [Semantic Versioning](https://semver.org/):
- **MAJOR** version when you make incompatible API changes
- **MINOR** version when you add functionality in a backwards compatible manner
- **PATCH** version when you make backwards compatible bug fixes

### Release Checklist
- [ ] Update version numbers in pom.xml files
- [ ] Update CHANGELOG.md with new version
- [ ] Run full test suite including benchmarks
- [ ] Update documentation
- [ ] Create GitHub release with release notes
- [ ] Deploy to Maven Central (if applicable)

## Getting Help

- **Documentation**: Check the README and wiki first
- **Issues**: Search existing issues before creating new ones
- **Discussions**: Use GitHub Discussions for general questions
- **Chat**: Join our community chat (link in README)

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (Apache 2.0).

## Recognition

Contributors who make significant contributions will be recognized:
- Listed in the README contributors section
- Mentioned in release notes for their contributions
- Added to the project's Hall of Fame

Thank you for contributing to Warmup Framework! 🚀