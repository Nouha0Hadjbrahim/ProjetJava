# -*- coding: utf-8 -*-
import face_recognition
import cv2
import sys
import os
import tempfile

# Répertoire absolu de l'image admin
base_dir = os.path.dirname(os.path.abspath(__file__))
reference_image_path = os.path.join(base_dir, "assets", "users", "admin_face.jpg")

# Étape 1 : Charger image admin
try:
    known_image = face_recognition.load_image_file(reference_image_path)
    known_encoding = face_recognition.face_encodings(known_image)[0]
except Exception:
    print("REFERENCE_ERROR")
    sys.exit()

# Étape 2 : Capture depuis webcam
video_capture = cv2.VideoCapture(0)
ret, frame = video_capture.read()
video_capture.release()

if not ret:
    print("CAMERA_ERROR")
    sys.exit()

# Étape 3 : Sauvegarde dans un fichier temporaire
with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as temp_file:
    capture_path = temp_file.name
    cv2.imwrite(capture_path, frame)

# Étape 4 : Traitement et comparaison
try:
    unknown_image = face_recognition.load_image_file(capture_path)
    encodings = face_recognition.face_encodings(unknown_image)

    if not encodings:
        print("NO_FACE_DETECTED")
        sys.exit()

    match_result = face_recognition.compare_faces([known_encoding], encodings[0])
    print("MATCH" if match_result[0] else "NO_MATCH")

except Exception as e:
    print("PROCESSING_ERROR")
    with open(os.path.join(base_dir, "face_id_error_log.txt"), "w", encoding="utf-8") as log_file:
        log_file.write("Erreur : " + str(e))
