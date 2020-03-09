/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package anywheresoftware.mongo;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Transformer;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonTypeCodecMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import anywheresoftware.b4a.objects.collections.Map.MyMap;

public class MapCodec implements CollectibleCodec<MyMap> {

    private static final String ID_FIELD_NAME = "_id";

    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final CodecRegistry registry;
    private final IdGenerator idGenerator;
    private final Transformer valueTransformer;

    /**
     * Construct a new instance with a default {@code CodecRegistry} and
     */
    public MapCodec() {
    	HashMap<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
    	replacements.put(BsonType.DOCUMENT, MyMap.class);
        this.registry = fromProviders(asList(new ValueCodecProvider(),new BsonValueCodecProvider(), new DocumentCodecProvider(),
        		new CodecProvider() {
					
					@Override
					 @SuppressWarnings("unchecked")
					public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
						if (clazz == MyMap.class)
							return (Codec<T>) MapCodec.this;
						return null;
					}
				}));
        this.bsonTypeCodecMap = new BsonTypeCodecMap(new BsonTypeClassMap(replacements), registry);
        this.idGenerator = new ObjectIdGenerator();
        this.valueTransformer = new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        };
    }


    

    @Override
    public boolean documentHasId(final MyMap document) {
        return document.containsKey(ID_FIELD_NAME);
    }

    @Override
    public BsonValue getDocumentId(final MyMap document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        Object id = document.get(ID_FIELD_NAME);
        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }

        BsonDocument idHoldingDocument = new BsonDocument();
        BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
        writer.writeStartDocument();
        writer.writeName(ID_FIELD_NAME);
        writeValue(writer, EncoderContext.builder().build(), id);
        writer.writeEndDocument();
        return idHoldingDocument.get(ID_FIELD_NAME);
    }

    @Override
    public MyMap generateIdIfAbsentFromDocument(final MyMap document) {
        if (!documentHasId(document)) {
            document.put(ID_FIELD_NAME, idGenerator.generate());
        }
        return document;
    }

  
    @Override
    public MyMap decode(final BsonReader reader, final DecoderContext decoderContext) {
    	MyMap document = new MyMap();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            document.put(fieldName, readValue(reader, decoderContext));
        }
        reader.readEndDocument();
        return document;
    }

    @Override
    public Class<MyMap> getEncoderClass() {
        return MyMap.class;
    }

    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final MyMap document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.containsKey(ID_FIELD_NAME)) {
            bsonWriter.writeName(ID_FIELD_NAME);
            writeValue(bsonWriter, encoderContext, document.get(ID_FIELD_NAME));
        }
    }

    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals(ID_FIELD_NAME);
    }

   
    
    @Override
    public void encode(final BsonWriter writer, final MyMap document, final EncoderContext encoderContext) {
        writeMap(writer, document, encoderContext);
    }


    private void writeMap(final BsonWriter writer, final MyMap map, final EncoderContext encoderContext) {
        writer.writeStartDocument();

        beforeFields(writer, encoderContext, map);

        for (final Map.Entry<Object, Object> entry : map.entrySet()) {
            if (skipField(encoderContext, (String)entry.getKey())) {
                continue;
            }
            writer.writeName((String)entry.getKey());
            writeValue(writer, encoderContext, entry.getValue());
        }
        writer.writeEndDocument();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
        if (value == null) {
            writer.writeNull();
        } else if (value instanceof Iterable) {
            writeIterable(writer, (Iterable<Object>) value, encoderContext.getChildContext());
        } else if (value instanceof Object[]) {
        	writeIterable(writer, Arrays.asList((Object[])value), encoderContext.getChildContext());
        } else if (value instanceof MyMap) {
            writeMap(writer, (MyMap) value, encoderContext.getChildContext());
        } else {
            Codec codec = registry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
    }

    private void writeIterable(final BsonWriter writer, final Iterable<Object> list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {
           return readList(reader, decoderContext);
        } else if (bsonType == BsonType.BINARY) {
            return registry.get(byte[].class).decode(reader, decoderContext);
        }
        return valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
    }

    private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return list;
    }
    public static class MyMapBson extends MyMap implements Bson {
    	public MyMapBson(MyMap m) {
    		super();
    		putAll(m);
    	}
		@Override
		public <TDocument> BsonDocument toBsonDocument(
				Class<TDocument> documentClass, CodecRegistry codecRegistry) {
			return new BsonDocumentWrapper<MyMap>(this, codecRegistry.get(MyMap.class));
		}
    	
    }
}
