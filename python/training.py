from ultralytics import YOLO
from roboflow import Roboflow
import argparse


argParser = argparse.ArgumentParser()
argParser.add_argument("-d", "--download", help="Download data and model")

args = argParser.parse_args()

if args.download:
    # download dataset in correct format
    rf = Roboflow(api_key="ROBOFLOW_API_KEY")
    project = rf.workspace("recicle").project("trash-jokie")
    dataset = project.version(1).download("yolov8")

model = YOLO("yolov8n.pt") # load pretrained model


"""
softlink the dataset into the ultralytics directory
probably because poetrys venv is in a different directory
"""
model.train(data="trash-1/data.yaml", epochs=3)  # train the model
metrics = model.val()  # evaluate model performance on the validation set
success = model.export() # export the model as pt fomat
