package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.CharacterType;
import models.Item;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

/**
 * This class controls the flow of Item game data
 *
 * @author Lowell Buttorff
 */
public class ItemController extends Controller {

    /**
     * HTTP Get request that returns all items in database
     *
     * @return Result Json list of items
     */
    public Result getAllItems() {
        List<Item> items;
        try {
            if(request().body().asJson().has("type")) {
                String type = request().body().asJson().get("type").textValue();
                items = Item.find.query().where().eq("type", type).findList();
            }else {
                items = Item.find.all();
            }
            return ok(Json.toJson(items));
        }catch (NullPointerException e) {
            return noContent();
        }
    }

    /**
     * HTTP Get request that returns a item from the database
     *
     * @param id The unique ID of a Item
     * @return Result Json of a item
     */
    public Result getItem(final long id) {
        Item item = Item.find.byId(id);
        if(item == null) {
            return noContent();
        }
        return ok(Json.toJson(item));
    }

    /**
     * HTTP Post request that creates a new Item
     *
     * @return Result Json of a item
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result createItem() {
        final JsonNode body = request().body().asJson();
        final String name = body.get("name").textValue();
        final String description = body.get("description").textValue();
        final String type = body.get("type").textValue();
        if(name == null || description == null || type == null) {
            return badRequest("Some data missing from request.");
        }
        if(Item.find.query().where().eq("name", name).findUnique() != null) {
            return badRequest("A item already exists with the name, "+name);
        }
        try {
            CharacterType characterType = CharacterType.valueOf(type);
            Item item = new Item(characterType, name, description);
            item.save();
            return ok(Json.toJson(item));
        }catch(Exception e) {
            Logger.info("An invalid CharacterType was supplied for updateItem");
            return badRequest("The CharacterType supplied was invalid");
        }
    }

    /**
     * HTTP Put request that updates an existing Item
     *
     * @param id The Unique ID of a Item
     * @return Result Json of a item
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateItem(final long id) {
        Item item = Item.find.byId(id);
        if(item == null) {
            return badRequest("There is no item with id, "+id);
        }
        JsonNode body = request().body().asJson();
        String newName = body.findPath("name").textValue();
        if(newName != null) {
            item.setName(newName);
        }
        String newDescription = body.findPath("description").textValue();
        if(newDescription != null) {
            item.setDescription(newDescription);
        }
        String newType = body.findPath("type").textValue();
        if(newName != null) {
            try{
                CharacterType newCharacterType = CharacterType.valueOf(newType);
                item.setType(newCharacterType);
            } catch(Exception e) {
                Logger.info("An invalid CharacterType was supplied for updateItem");
                return badRequest("The CharacterType supplied was invalid");
            }
        }
        item.save();
        return ok(Json.toJson(item));
    }

    /**
     * HTTP Delete request that deletes a Item from the database
     *
     * @param id The Unique ID of a Item
     * @return Result Json of a item
     */
    public Result deleteItem(final long id) {
        Item item = Item.find.byId(id);
        if(item == null) {
            return badRequest("There is no item with id, "+id);
        }
        item.delete();
        return ok(Json.toJson(item));
    }
}