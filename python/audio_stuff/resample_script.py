import librosa
import resampy
import soundfile as sf

# Load audio file
audio_file = 'resampled_audio_files/cut_sample_8.wav'
y, sr = librosa.load(audio_file)

# Resample to 16 kHz
y_resampled = resampy.resample(y, sr, 16000)

# Save resampled audio to new file
resampled_file = 'resampled_audio_files/cut_sample_8.wav'
sf.write(resampled_file, y_resampled, 16000)


