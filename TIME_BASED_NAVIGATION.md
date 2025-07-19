# Time-Based Navigation in Immich-TV

This document explains how the time-based navigation feature works in Immich-TV.

## Navigation Modes

The app supports 5 different navigation modes:

1. **Photo by Photo** (Default) - Navigate one photo at a time
2. **Day by Day** - Jump to photos from the previous/next day
3. **Week by Week** - Jump to photos from the previous/next week  
4. **Month by Month** - Jump to photos from the previous/next month
5. **Year by Year** - Jump to photos from the previous/next year

## How to Use

1. **Changing Navigation Mode**: 
   - Press the **MENU** button on your remote control to cycle through navigation modes
   - The current mode is displayed in the top-left corner of the screen

2. **Navigating**: 
   - Use **DPAD_DOWN** (down arrow) to go forward in time based on the selected mode
   - Use **DPAD_UP** (up arrow) to go backward in time based on the selected mode
   - Use **DPAD_LEFT/RIGHT** for horizontal navigation (also time-based when not in Photo by Photo mode)

3. **Date Context**:
   - The current date context is shown in the top-right corner
   - Format changes based on navigation mode:
     - Photo by Photo: "Dec 25, 2023"
     - Day by Day: "Dec 25, 2023" 
     - Week by Week: "Week 52, 2023"
     - Month by Month: "December 2023"
     - Year by Year: "2023"

## Examples

- If you're viewing a photo from December 2023 and have **Month by Month** selected:
  - Pressing **DOWN** will jump to a photo from January 2024 (if available)
  - Pressing **UP** will jump to a photo from November 2023 (if available)

- If you have **Week by Week** selected:
  - Pressing **DOWN** will jump to photos from the next week
  - Pressing **UP** will jump to photos from the previous week

## Technical Details

- The navigation finds the closest photo by date when jumping between time periods
- If no photos exist for the target time period, it finds the nearest available photos
- Date information is extracted from EXIF data (`dateTimeOriginal`) or file modification time (`fileModifiedAt`)
- Navigation respects the current photo sorting and filtering settings