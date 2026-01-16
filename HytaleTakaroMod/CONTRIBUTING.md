# Contributing to Hytale-Takaro Integration

Thank you for your interest in contributing! This project integrates Hytale servers with the Takaro platform.

## Getting Started

1. **Fork the repository**
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Hytale-Takaro-Integration.git
   cd Hytale-Takaro-Integration
   ```
3. **Set up development environment**:
   - Java 24+
   - Maven 3.6+
   - HytaleServer.jar (copy to `../libs/`)

## Making Changes

1. **Create a branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Follow existing code style
   - Add comments for complex logic
   - Update documentation if needed

3. **Test your changes**:
   - Build: `mvn clean package`
   - Test in a Hytale server
   - Verify Takaro connection

4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Add feature: description"
   ```

5. **Push and create PR**:
   ```bash
   git push origin feature/your-feature-name
   ```
   Then open a Pull Request on GitHub.

## Code Guidelines

- **Java Style**: Follow standard Java conventions
- **Comments**: Document public methods and complex logic
- **Error Handling**: Always catch and log exceptions
- **Logging**: Use `plugin.getLogger()` for all logging

## Areas to Contribute

### High Priority
- [ ] Implement actual Hytale event registration
- [ ] Add real Hytale server API calls (getPlayers, kick, ban, etc.)
- [ ] Test Hytale first-party API endpoints
- [ ] Add unit tests

### Nice to Have
- [ ] Configuration GUI
- [ ] Additional Takaro event types
- [ ] Performance optimizations
- [ ] Multi-language support

## Reporting Issues

When reporting bugs, please include:
- Hytale server version
- Plugin version
- Steps to reproduce
- Error logs (if applicable)
- Config file (remove tokens!)

## Questions?

- Open an issue for questions
- Check existing issues first
- Be respectful and constructive

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for helping make Hytale-Takaro Integration better! 🚀
