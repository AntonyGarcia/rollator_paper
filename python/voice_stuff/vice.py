import json
import struct
import speech_recognition as sr

# read data from JSON file
with open('values.json', 'r') as f:
    data = json.load(f)

# convert data to audio data (int16 data with a sample rate of 16kHz)
audio = struct.pack('<' + str(len(data)) + 'h', *data)

# create an AudioData object
audio_data = sr.AudioData(audio, 16000, 2)

# create a recognizer object
r = sr.Recognizer()

# use Google's speech recognition API to transcribe the audio
text = r.recognize_google(audio_data)

# print the transcribed text
print(text)
