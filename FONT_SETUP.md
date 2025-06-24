# Font Setup Instructions

## Adding Bebas Neue Font

To complete the Bebas Neue font setup, you need to add the actual font file:

### Step 1: Download Bebas Neue Font
1. Go to [Google Fonts - Bebas Neue](https://fonts.google.com/specimen/Bebas+Neue)
2. Click "Download family" button
3. Extract the downloaded ZIP file

### Step 2: Add Font File
1. From the extracted folder, copy `BebasNeue-Regular.ttf`
2. Rename it to `bebas_neue_regular.ttf` (lowercase, underscores)
3. Place it in this directory: `app/src/main/res/font/`

### Step 3: Verify Setup
The font is already configured in the app's typography system. Once you add the TTF file:
- App name "Kharrency" will use Bebas Neue
- All headings and titles will use Bebas Neue
- Body text will remain with the default font for readability

### File Structure Should Look Like:
```
app/src/main/res/font/
├── bebas_neue_regular.ttf          (ADD THIS FILE)
└── bebas_neue_font_family.xml      (already created)
```

### Alternative Download Sources:
- [Font Squirrel](https://www.fontsquirrel.com/fonts/bebas-neue)
- [DaFont](https://www.dafont.com/bebas-neue.font)

Make sure the file is exactly named `bebas_neue_regular.ttf` for the app to recognize it.

### Quick Download Command (if you have curl):
```bash
# Download directly to the font folder
curl -L "https://github.com/google/fonts/raw/main/ofl/bebasneue/BebasNeue-Regular.ttf" -o app/src/main/res/font/bebas_neue_regular.ttf
``` 