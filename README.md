# Roxio Plugin Repo Template

Welcome to the **[Roxio](https://github.com/Recoo31/Rokko)** Plugin Repository Template! This template is designed to help you get started with writing your first Roxio plugin with ease.

## Getting Started

This repository includes a template and an example plugin to guide you through the process.

### 1. Create a New Android Library

Begin by creating a new Android library in your project. This will be the foundation of your plugin.

### 2. Configure the Build Script

Next, locate the library's `build.gradle.kts` file and add the necessary configurations:

- Add the dependencies from [here](https://github.com/Recoo31/PluginTemplate/blob/752e1fb21905da49794f2fcbb381430919d4e923/BluTv/build.gradle.kts#L40-L116).
- Add the gradle task from [here](https://github.com/Recoo31/PluginTemplate/blob/752e1fb21905da49794f2fcbb381430919d4e923/BluTv/build.gradle.kts#L129-L131).

### 3. Build or Deploy Your Plugin

Use the following commands to build or deploy your plugin:

- **Windows:** `.\gradlew.bat Example:make`
- **Linux & Mac:** `./gradlew Example:make`

### 4. After Build

Once the build is complete, locate the generated file in the `xxx\Example\build\xxx.krd` directory. Finally, create a new GitHub repository similar to [this one](https://github.com/Recoo31/RoxioPlugins) to host your plugin.

## Additional Resources

- [Take a look](https://github.com/Recoo31/Rokko)