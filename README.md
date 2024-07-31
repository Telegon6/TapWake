# Eventicker

Eventicker is an Android application that allows users to create and manage alarms and events using NFC tags. With Eventicker, you can write alarm and event data to NFC tags and later read this data to set up alarms or reminders.

## Features

- Create alarms with customizable time, date, and description
- Set up events with title, date, time, and description
- Select custom alarm sounds
- Write alarm and event data to NFC tags
- Read alarm and event data from NFC tags
- User-friendly interface with material design

## Requirements

- Android device with NFC capability
- Android 5.0 (Lollipop) or higher
- NFC tags (NTAG203, NTAG213, or similar)

## Installation

1. Clone this repository or download the ZIP file
2. Open the project in Android Studio
3. Build and run the app on your Android device

## Usage

### Writing to NFC Tag

1. Open the Eventicker app
2. Choose to create either an alarm or an event
3. Fill in the required details (time, date, description, etc.)
4. Tap the "Write to NFC" button
5. When prompted, hold an NFC tag against the back of your device
6. Wait for the confirmation message

### Reading from NFC Tag

1. Open the Eventicker app
2. Navigate to the "Read NFC" section
3. Hold an NFC tag (previously written with Eventicker) against the back of your device
4. The app will read the data and display the alarm or event details

## Permissions

Eventicker requires the following permissions:

- NFC: to read and write NFC tags
- Ring: for alarm notifications
- Receive Boot Completed: to restore alarms after device reboot

## Contributing

Contributions to Eventicker are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

- [Android NFC Documentation](https://developer.android.com/guide/topics/connectivity/nfc)
- [Material Design](https://material.io/design)

## Contact

If you have any questions, issues, or suggestions, please open an issue in this repository.

Thank you for using Eventicker!
