# TrashTalk App

An MVP for TrashTalk with some core functionalities working.
1. List of recyclable material
2. Detailed information about recyclable material
3. Scanning and identifying of materials
4. Map of recycling locations

The scanning component uses a custom trained AI object recognition model (based on yolo) to identify recyclables.
The map is a custom integration of OSM, in which recycling locations are shown.
Included with the map is also an AI routing system provided by OSM which guides the user to the closest recycling station.

Use cases
Opening the app shows tiles of all recyclables and by selecting one, it shows further information about the specific recyclable.

After selecting a recyclable the button "Take me to recycling station" is available and by pressing it the nearest station where this type of recyclable is accepted will be show on the map and a route displayed.

Opening the Scan function allows the user to scan their product and afterwards it shows the type of recyclable it is. There the button "Take me to recycling station" is available again.