# ActivityIsTheKey
## Overall description
This sample project presents a simple app that tracks the activity made for the user when he is walking or running. For the activity detection, relies on Android's AwarenessApi. The was of measuring the user activity is just counting the time the user is wunning or walking until he reaches a threshold value. 

The code is structured following MVP, where the View is MainActivity. A repository is responsible from handling persistence and storing the time the user has been perfoming an activity. This repository has been created based in SharedPreferences for simplicity, but since it is using an abstraction it would be very easy to exange for a different one, using Romm i.e. Repository follow s a reactive principla, so component could register for changes in the database. 

This is the mission of the following components.

## MainActivity /MainPresenter
Counts the time the user is performing an activity and, while thisis happening, dispays a fragment to indicate that activity is bine tracked. Main presenter is responsible from save activity data through the repository.

## Status fragment
Display overall status of the activity of the user and how close is from the threshold. This fragment subscribes to the repository for updates, reacting to changes in other parts of the app.

## InActivityFragment
Displays the current activity performed by the user.

## Problems encountered
My initial test with the Awareness API were promissing, but in the end it resulted in random behavior wher eno explanation could be found in the docs. Not even the sample code from Google was working properly in the Google Pixel, but the lack of time force for a plan B. The initial case were way more complex that the final one, trying to track three different activities, but was limited to just "walking or running" to present something more stable.

UI was much nicer taking into account that three activities were being tracked, but it has to be downgraded to a simple Progreessbar for simplicity.

## Improvements for the future
The time expended trying to fix/understand the Awareness Api behavior could have been better invested in:
 - Creating an alternate repository based in Room.
 - Add an small backed to save data from the users and develop an small app to display a live summary of the total of tracked activity. Repository would be responsible to upload the data to the server in the background.
 - Add initial section for explanation
 - add Congratulations screen once the activity threshold has been achieved
