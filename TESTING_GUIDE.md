# Manual Testing Guide for Time-Based Navigation

This guide helps test the time-based navigation feature after implementing the fix.

## Prerequisites
- Android TV device or emulator
- Immich-TV app installed with the fix
- Photo library with photos from different dates/times
- Remote control for navigation

## Test Cases

### Test Case 1: Navigation Mode Switching
**Objective**: Verify that navigation modes can be changed using the MENU button

**Steps**:
1. Open any photo grid (Recent, All Photos, etc.)
2. Press **MENU** button on remote
3. Observe navigation mode text in top-left corner
4. Press **MENU** again
5. Verify mode cycles through: Photo by Photo → Day by Day → Week by Week → Month by Month → Year by Year → Photo by Photo

**Expected Result**: Navigation mode text updates correctly with each MENU press

### Test Case 2: Photo by Photo Navigation (Default)
**Objective**: Verify default navigation works normally

**Steps**:
1. Ensure navigation mode is "Photo by Photo"
2. Press **DPAD_DOWN** (down arrow)
3. Press **DPAD_UP** (up arrow)
4. Press **DPAD_LEFT** and **DPAD_RIGHT**

**Expected Result**: Normal grid navigation (one photo at a time)

### Test Case 3: Month by Month Navigation
**Objective**: Verify month-by-month navigation works as described in the issue

**Steps**:
1. Change navigation mode to "Month by Month" using MENU
2. Note the current photo date in top-right corner
3. Press **DPAD_DOWN** (down arrow)
4. Check if navigation jumped to photos from the next month
5. Press **DPAD_UP** (up arrow) 
6. Check if navigation jumped to photos from the previous month

**Expected Result**: 
- Navigation jumps month by month instead of photo by photo
- Date context in top-right shows month changes (e.g., "December 2023" → "January 2024")

### Test Case 4: Day by Day Navigation
**Objective**: Verify daily navigation

**Steps**:
1. Change navigation mode to "Day by Day"
2. Note current date
3. Press **DPAD_DOWN** and **DPAD_UP**

**Expected Result**: Navigation jumps day by day, date context shows daily changes

### Test Case 5: Week by Week Navigation
**Objective**: Verify weekly navigation

**Steps**:
1. Change navigation mode to "Week by Week"
2. Note current date context (should show "Week X, YYYY")
3. Press **DPAD_DOWN** and **DPAD_UP**

**Expected Result**: Navigation jumps week by week, date context shows "Week 23, 2023" format

### Test Case 6: Year by Year Navigation
**Objective**: Verify yearly navigation

**Steps**:
1. Change navigation mode to "Year by Year"
2. Note current date context (should show just year "2023")
3. Press **DPAD_DOWN** and **DPAD_UP**

**Expected Result**: Navigation jumps year by year, date context shows year changes

## Debug Information

If testing on a device with USB debugging enabled, you can check the logs:

```bash
adb logcat | grep "Time-based navigation\|Handling navigation key"
```

Look for log messages like:
- "Handling navigation key event: keyCode=20, mode=Month by Month"
- "Time-based navigation: direction=1, currentIndex=5, targetIndex=25, mode=Month by Month"

## Troubleshooting

### Issue: Navigation mode doesn't change
- Verify MENU button is working
- Check if popup menu opens instead (might need to adjust key handling)

### Issue: Time-based navigation not working
- Check debug logs for key event handling
- Verify navigation mode is not "Photo by Photo"
- Ensure photos have valid date information (EXIF or file modification times)

### Issue: Navigation jumps to wrong photos
- Check if photo sorting affects the date-based search
- Verify date calculations are correct for the specific time period

## Expected Debug Log Output

When working correctly, you should see logs like:
```
D/VerticalCardGridFragment: Handling navigation key event: keyCode=20, mode=Month by Month
D/VerticalCardGridFragment: Time-based navigation: direction=1, currentIndex=10, targetIndex=45, mode=Month by Month
```

## Success Criteria

The fix is successful when:
1. ✅ Navigation mode can be changed with MENU button
2. ✅ "Photo by Photo" mode works normally (default LeanBack navigation)
3. ✅ "Month by Month" mode jumps month-by-month when pressing DPAD_DOWN/UP
4. ✅ Other time modes (day, week, year) work similarly
5. ✅ Date context in UI updates correctly
6. ✅ Navigation respects photo date information correctly