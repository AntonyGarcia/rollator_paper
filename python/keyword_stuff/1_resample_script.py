import librosa
import resampy
import soundfile as sf

# Load audio file
audio_file = 'raw_samples/sound.mp3'
y, sr = librosa.load(audio_file)

# Resample to 16 kHz
y_resampled = resampy.resample(y, sr, 16000)

# Save resampled audio to new file
resampled_file = 'resampled_audio_files/sample_1.wav'
sf.write(resampled_file, y_resampled, 16000)


