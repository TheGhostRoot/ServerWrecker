import config from 'generator-config.json'
import mcDataPackage from 'minecraft-data'
import fs from 'fs'
import process from 'process'

const mcData = mcDataPackage(config.version)
const enumReplace = "// VALUES REPLACE"

if (mcData == null) {
  console.error(`Version ${config.version} not found`)
  process.exit(1)
} else {
  const getNameOfItemId = (id: number): string | null => {
    return mcData.items[id].name.toUpperCase();
  }
  const getNameOfShapeId = (id: number): string | null => {
    return "SHAPE_" + id
  }

  fs.mkdirSync("output", {recursive: true})

  {
    let enumValues: string[] = []
    for (const item of Object.keys(mcData.blockCollisionShapes.shapes)) {
      const id = Number(item)
      const shape = mcData.blockCollisionShapes.shapes[item as any]

      let shapeData = item
      const shapeList: string[] = []
      for (const shapePart of shape) {
        shapeList.push(`${shapePart[0]},${shapePart[1]},${shapePart[2]},${shapePart[3]},${shapePart[4]},${shapePart[5]}`)
      }
      if (shapeList.length > 0) {
        shapeData += "|"
      }
      shapeData += shapeList.join("|")

      enumValues.push(shapeData)
    }

    fs.writeFileSync("output/blockshapes.txt", enumValues.join("\n"))
  }

  {
    let result = fs.readFileSync("templates/BlockType.java", "utf-8");
    let enumValues: string[] = []
    for (const block of mcData.blocksArray) {
      let harvestData: string | null = "List.of("
      if (block.harvestTools) {
        const harvestTools = block.harvestTools;
        const toolList: string[] = []
        for (const tool of Object.keys(harvestTools)) {
          toolList.push(`ItemType.${getNameOfItemId(Number(tool))}`)
        }
        harvestData += toolList.join(", ")
      }
      harvestData += ")"

      let defaultShape = 0
      const collisionShapes = mcData.blockCollisionShapes.blocks[block.name]
      if (collisionShapes) {
        // noinspection SuspiciousTypeOfGuard
        if (typeof collisionShapes === "number") {
          defaultShape = collisionShapes
        } else {
          defaultShape = collisionShapes[0]
        }
      }

      enumValues.push(`public static final BlockType ${block.name.toUpperCase()} = register(new BlockType(${block.id}, "${block.name}", "${block.displayName}", ${block.hardness ?? -1}, ${block.resistance}, ${block.stackSize}, ${block.diggable}, BoundingBoxType.${block.boundingBox.toUpperCase()}, ${harvestData}, BlockShapeType.getById(${defaultShape})));`)
    }

    result = result.replace(enumReplace, enumValues.join("\n    "))

    fs.writeFileSync("output/BlockType.java", result)
  }

  {
    let result = fs.readFileSync("templates/ItemType.java", "utf-8");
    let enumValues: string[] = []
    for (const item of mcData.itemsArray) {
      enumValues.push(`public static final ItemType ${item.name.toUpperCase()} = register(new ItemType(${item.id}, "${item.name}", "${item.displayName}", ${item.stackSize}, ${stringArrayToJavaList(item.enchantCategories)}, ${stringArrayToJavaList(item.repairWith)}, ${item.maxDurability ?? "-1"}));`)
    }

    result = result.replace(enumReplace, enumValues.join("\n    "))

    fs.writeFileSync("output/ItemType.java", result)
  }

  {
    let result = fs.readFileSync("templates/EntityType.java", "utf-8");
    let enumValues: string[] = []
    for (const item of mcData.entitiesArray) {
      enumValues.push(`public static final EntityType ${item.name.toUpperCase()} = register(new EntityType(${item.id}, ${item.internalId}, "${item.name}", "${item.displayName}", "${item.type}", ${item.width}, ${item.height}, ${valueToNullStringFallback(-1, item.length)}, ${valueToNullStringFallback(-1, item.offset)}, "${item.category}"));`)
    }

    result = result.replace(enumReplace, enumValues.join("\n    "))

    fs.writeFileSync("output/EntityType.java", result)
  }

  {
    let result = fs.readFileSync("templates/FoodType.java", "utf-8");
    let enumValues: string[] = []
    for (const food of mcData.foodsArray) {
      enumValues.push(`public static final FoodType ${food.name.toUpperCase()} = register(new FoodType(ItemType.${getNameOfItemId(food.id)}, ${food.foodPoints}, ${food.saturation}, ${food.effectiveQuality}, ${food.saturationRatio}));`)
    }

    result = result.replace(enumReplace, enumValues.join("\n    "))

    fs.writeFileSync("output/FoodType.java", result)
  }
}

function stringArrayToJavaList(array?: string[]): string {
  if (array == null) {
    return "null"
  }

  return `List.of(${array.map(data => `"${data}"`).join(", ")})`
}

function valueToNullStringFallback(fallback: any, array?: any): string {
  if (array == null) {
    return fallback
  }

  return `"${array}"`
}
