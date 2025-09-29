# PlantSketch 🌱

An ecological simulation application that models plant species growth and viability under various environmental conditions. PlantSketch provides comprehensive tools for setting up, running, and analyzing ecological simulations with an intuitive JavaFX GUI.

## Quick Start

To run the application:

```bash
mvn clean compile javafx:run
```
## Repository
https://gitlab.cs.uct.ac.za/capstone-project-PlantSketch/plantsketch/-/tree/main

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
    - [Launch Screen](#launch-screen)
    - [Test Mode](#test-mode)
    - [Run Mode](#run-mode)
    - [3D Visualization](#3d-visualization)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Features

-  **Test Mode** - Controlled 2x2 grid environment for algorithm testing
- **Run Mode** - Real environmental datasets with multiple resolutions (256-1024px)
-  **3D Visualization** - OpenGL-based 3D rendering of ecological simulations
-  **7 Plant Species** - Boxwood, Snowy Mespilus, Mountain Pine, Silver Fir, Silver Birch, Sessile Oak, European Beech
-  **Interactive Brush Tools** - Direct manipulation of plant populations and environmental conditions
-  **Undo/Redo System** - Full simulation state management
-  **Multiple Visualizations** - Environment, sunlight, temperature, moisture, age, and forest views
-  **Data Export** - Save simulation states as .pdb files for external analysis

## Prerequisites

- **Java JDK** 11 or higher
- **Apache Maven** 3.6.0 or higher
- **JavaFX SDK** 17 or higher

## Installation

1. Clone the repository:
```bash
git clone 
```

2. Install dependencies:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn clean compile javafx:run
```

## Usage

### Launch Screen

Upon launching PlantSketch, you'll see three main modes:

| Mode | Description | Use Case |
|------|-------------|----------|
| **Test Mode** | 2x2 grid with manual parameters | Algorithm testing and parameter sensitivity analysis |
| **Run Mode** | Real environmental datasets | Realistic ecological modeling |
| **3D Visualization** | OpenGL rendering | Visual exploration of simulation results |

### Test Mode

Test Mode provides controlled environment testing with a simplified 2x2 grid.

#### Key Features

**Visualization Tabs:**
- Environment (Elevation) with plant overlay
- Sunlight distribution
- Temperature patterns
- Moisture levels
- Age cohort information
- Pink noise sampling
- Forest view

**Environmental Parameters:**
- Temperature (°C)
- Age (years)
- Moisture (%)
- Sunlight (hours)
- Elevation (m)
- Slope (degrees) - auto-calculated

**Species Parameters:**
Each species has adjustable parameters for:
- Environmental tolerances (sunlight, moisture, temperature, slope)
- Growth characteristics (max height, growth rate, lifespan)

### Run Mode

Advanced simulation with real environmental datasets and interactive manipulation tools.

#### Interactive Brush System

**Three Brush Modes:**

1. **Removal Brush** - Remove selected species from areas
2. **Age Brush** - Modify plant ages in target areas
3. **Abiotic Brush** - Adjust environmental conditions

**Brush Controls:**
- Size adjustment (1-9 units)
- Toggle buttons for mode activation
- Visual feedback with crosshair cursor
- Tab-specific functionality

#### Environmental Adjustments

Real-time sliders for modifying:
- Temperature (±0.1°C)
- Age (±100 years)
- Sunlight (±0.1 hours)
- Moisture (±6%)

#### Species Management

- Color-coded species selection
- Visibility toggles for each species
- "Selected Species Only" filtering
- Parameter editing via accordion panels

### State Management

#### Undo/Redo System
- Maximum 10 saved states
- Automatic state creation on major changes
- Full simulation history navigation

#### Data Export
```bash
# Export format
filename.pdb
```
Contains complete simulation state for external analysis tools.

## 3D Visualization

Select a file that starts with D-... that contains a .pdb file and press run
## Architecture

```
plantsketch/
│
├── src/main/java/
│   ├── controllers/
│   │   ├── ModeController.java
│   │   ├── TestController.java
│   │   └── RunController.java
│   ├── models/
│   │   ├── Species.java
│   │   ├── Environment.java
│   │   └── SimulationState.java
│   ├── simulation/
│   │   ├── EcologicalEngine.java
│   │   └── PinkNoiseGenerator.java
│   └── visualization/
│       ├── HeatmapRenderer.java
│       └── OpenGLRenderer.java
│
├── src/main/resources/
│   ├── fxml/
│   ├── css/
│   └── data/
│
└── pom.xml
```

## Configuration

### Maven Configuration

```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <javafx.version>17.0.2</javafx.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.plantsketch.Main</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Environmental Data Format

Custom datasets should include:
- Elevation data (meters)
- Temperature data (°C)
- Moisture data (%)
- Sunlight data (hours)

Place files in a folder and select "Custom Folder" option at startup.

## Performance Optimization

### Recommended Settings

- **Zoom Level**: Start at 100%, adjust based on analysis needs
- **Species Selection**: Disable unnecessary species for better performance
- **Brush Size**: Use smaller brushes for precision, larger for bulk operations
- **State Management**: Save states before major parameter changes

### Performance Monitoring

Access performance stats via toolbar to view:
- Operation timing breakdown
- Execution bottlenecks
- Average operation durations

## Troubleshooting

### Common Issues

**Issue**: Simulation runs slowly
- **Solution**: Reduce active species count, lower zoom level

**Issue**: Brush tools not working
- **Solution**: Ensure correct tab is selected (not Pink Noise or Forest)

**Issue**: Cannot load custom data
- **Solution**: Verify all required environmental files are present

**Issue**: Memory errors with large datasets
- **Solution**: Increase JVM heap size: `mvn clean compile javafx:run -Djavafx.args="-Xmx4G"`

