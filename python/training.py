from ultralytics import YOLO
from roboflow import Roboflow
import argparse


argParser = argparse.ArgumentParser()
argParser.add_argument(
    "-d",
    "--download",
    help="Download data and model",
)

args = argParser.parse_args()

if args.download:
    # download dataset in correct format
    rf = Roboflow(api_key="UQNGehSMQGhMLjn65qi9")
    project = rf.workspace("recicle").project("trash-jokie")
    dataset = project.version(2).download("yolov8")

model = YOLO("yolov8n.pt")  # load pretrained model

"""
softlink the dataset into the ultralytics directory
probably because poetrys venv is in a different directory
might need to change ultralytics settings dataset path

use correct python and dataset version in following commands

cd .venv/lib/python3.8/site-packages/ultralytics/datasets
ln -s ../../../../../../trash-2/ .
"""
model.train(
    data="trash-2/data.yaml",
    model="yolov8n.pt",
    imgsz=640,
    epochs=1000000,
    save_period=100,
    device=0,
    optimizer="Adam",
)  # train the model
"""
cannot use classification model, dataset is not in correct format probably
model.train(
    data="trash-2",
    model="yolov8n-cls.pt",
    imgsz=640,
    epochs=2,
    device=0,
    optimizer="Adam",
    single_cls=True,
    dropout=0.1
)
"""
metrics = model.val()  # evaluate model performance on the validation set
success = model.export()  # export the model as pt fomat
