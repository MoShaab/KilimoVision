import os
from tflite_support import metadata as _metadata
from tflite_support import metadata_schema_py_generated as _metadata_fb

# Path to your TFLite model
MODEL_PATH = "KilimoVision.tflite"
OUTPUT_PATH = "KilimoVision_with_metadata.tflite"

# Labels for the model's output classes
LABELS = [
    "Tomato___Bacterial_spot",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___Leaf_Mold",
    "Tomato___Septoria_leaf_spot",
    "Tomato___Spider_mites Two-spotted_spider_mite",
    "Tomato___Target_Spot",
    "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "Tomato___Tomato_mosaic_virus",
    "Tomato___healthy"
]

def create_metadata():
    """Creates metadata for the tomato disease classification model."""
    # Create model info
    model_meta = _metadata_fb.ModelMetadataT()
    model_meta.name = "KilimoVision Tomato Disease Classifier"
    model_meta.description = ("Identifies the disease present on a tomato plant leaf "
                             "from an image.")
    model_meta.version = "v1.0.0"
    model_meta.author = "KilimoVision Team"
    model_meta.license = "Apache License. Version 2.0"

    # Input tensor metadata
    input_meta = _metadata_fb.TensorMetadataT()
    input_meta.name = "input_image"
    input_meta.description = "Input image to be classified. 224x224 RGB image."
    input_meta.content = _metadata_fb.ContentT()
    input_meta.content.contentProperties = _metadata_fb.ImagePropertiesT()
    input_meta.content.contentProperties.colorSpace = _metadata_fb.ColorSpaceType.RGB
    input_meta.content.contentPropertiesType = _metadata_fb.ContentProperties.ImageProperties

    # Define normalization for the input
    input_normalization = _metadata_fb.ProcessUnitT()
    input_normalization.optionsType = _metadata_fb.ProcessUnitOptions.NormalizationOptions
    input_normalization.options = _metadata_fb.NormalizationOptionsT()
    input_normalization.options.mean = [0.0, 0.0, 0.0]  # No mean subtraction
    input_normalization.options.std = [255.0, 255.0, 255.0]  # Divide by 255
    input_meta.processUnits = [input_normalization]

    input_stats = _metadata_fb.StatsT()
    input_stats.max = [255.0, 255.0, 255.0]
    input_stats.min = [0.0, 0.0, 0.0]
    input_meta.stats = input_stats

    # Output tensor metadata
    output_meta = _metadata_fb.TensorMetadataT()
    output_meta.name = "disease_prediction"
    output_meta.description = "Probabilities of the 10 disease classes."
    output_meta.content = _metadata_fb.ContentT()
    output_meta.content.contentPropertiesType = (_metadata_fb.ContentProperties.FeatureProperties)
    output_meta.content.contentProperties = _metadata_fb.FeaturePropertiesT()

    # Add labels file as associated file
    label_file = _metadata_fb.AssociatedFileT()
    label_file.name = "labels.txt"
    label_file.description = "Labels for tomato disease categories."
    label_file.type = _metadata_fb.AssociatedFileType.TENSOR_AXIS_LABELS
    output_meta.associatedFiles = [label_file]

    # Create subgraph metadata
    subgraph = _metadata_fb.SubGraphMetadataT()
    subgraph.inputTensorMetadata = [input_meta]
    subgraph.outputTensorMetadata = [output_meta]

    # Add model information to schema
    model_meta.subgraphMetadata = [subgraph]

    # Create metadata buffer and file
    b = _metadata.MetadataPopulator.create_metadata_buffer(model_meta)

    # Create labels file
    with open('labels.txt', 'w') as f:
        f.write('\n'.join(LABELS))

    # Populate metadata and labels
    populator = _metadata.MetadataPopulator.with_model_file(MODEL_PATH)
    populator.load_metadata_buffer(b)
    populator.load_associated_files(["labels.txt"])
    populator.populate()

    # Save output model
    try:
        populator.save_model_with_metadata(OUTPUT_PATH)
        print(f"Metadata added successfully to {OUTPUT_PATH}")

        # Verify the metadata
        displayer = _metadata.MetadataDisplayer.with_model_file(OUTPUT_PATH)
        print("\nMetadata and Associated Files:")
        print(displayer.get_metadata_json())
        print("\nAssociated files:")
        print(displayer.get_packed_associated_file_list())
    except Exception as e:
        print(f"Error saving model with metadata: {e}")
        # Fallback option: just create the labels file
        print("Creating labels.txt file only")

if __name__ == "__main__":
    # Ensure you have tflite-support package installed
    print("Beginning metadata addition process...")
    try:
        create_metadata()
    except Exception as e:
        print(f"Error creating metadata: {e}")
        # Create labels file as a fallback
        with open('labels.txt', 'w') as f:
            f.write('\n'.join(LABELS))
        print("Created labels.txt file only")