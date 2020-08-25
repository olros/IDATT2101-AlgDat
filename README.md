# IDATT2021 AlgDat

Exercises in IDATT2021 (Algorithm's og Datastructures) written in Java and C

### Compile C with WSL
To compile and run C with WSL (Windows Subsystem for Linux), run these commands in CMD:

```
wsl

// Option 1:
gcc [Filename].c [linking flags] && ./a.out

// Option 2:
gcc -o [OutputFilename] [Filename].c [linking flags]
./[OutputFilename]
```

You must have downloaded `gcc` for this to work. You can check that you have installed `gcc` by typing: `wsl gcc --version`

#### Linking flags examples
- `<math.h>`: `-lm`:
