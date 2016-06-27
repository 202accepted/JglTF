/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2016 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jgltf.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import de.javagl.jgltf.impl.Buffer;
import de.javagl.jgltf.impl.BufferView;
import de.javagl.jgltf.impl.GlTF;
import de.javagl.jgltf.impl.Image;
import de.javagl.jgltf.impl.Shader;
import de.javagl.jgltf.model.io.Buffers;

/**
 * A class storing a {@link GlTF} and the associated (binary) data that was 
 * read from external sources linked in the {@link GlTF}, or from the 
 * glTF file itself for the case of binary or embedded glTFs.
 */
public final class GltfData
{
    /**
     * The {@link GlTF}
     */
    private final GlTF gltf;
    
    /**
     * The {@link GlTF#getBuffers()}, as byte buffers
     */
    private final Map<String, ByteBuffer> bufferDatas;

    /**
     * The {@link GlTF#getBufferViews()}, as byte buffers
     */
    private final Map<String, ByteBuffer> bufferViewDatas;

    /**
     * The {@link GlTF#getImages()}, as byte buffers containing the raw data
     */
    private final Map<String, ByteBuffer> imageDatas;

    /**
     * The {@link GlTF#getShaders()}, as byte buffers containing the raw data
     */
    private final Map<String, ByteBuffer> shaderDatas;
    
    /**
     * Creates the new {@link GltfData}. 
     * 
     * @param gltf The {@link GlTF}
     */
    public GltfData(GlTF gltf)
    {
        this.gltf = gltf;
        
        this.bufferDatas = new ConcurrentHashMap<String, ByteBuffer>();
        this.bufferViewDatas = new ConcurrentHashMap<String, ByteBuffer>();
        this.imageDatas = new ConcurrentHashMap<String, ByteBuffer>();
        this.shaderDatas = new ConcurrentHashMap<String, ByteBuffer>();
    }
    
    /**
     * Copy the data from the given {@link GltfData} into this one. This
     * will establish all ID-to-data mappings that exist in the given 
     * {@link GltfData} in this one (overwriting any existing mappings).
     * The data elements will be copied by reference (that is, it will
     * not create a deep copy of the data elements).
     * 
     * @param that The {@link GltfData} to copy
     */
    public void copy(GltfData that)
    {
        this.bufferDatas.putAll(that.bufferDatas);
        this.bufferViewDatas.putAll(that.bufferViewDatas);
        this.imageDatas.putAll(that.imageDatas);
        this.shaderDatas.putAll(that.shaderDatas);
    }
    
    /**
     * Returns the {@link GlTF}
     * 
     * @return The {@link GlTF}
     */
    public GlTF getGltf()
    {
        return gltf;
    }

    /**
     * Store the given byte buffer under the given {@link Shader} id
     * 
     * @param id The {@link Shader} ID
     * @param byteBuffer The byte buffer containing the shader data
     */
    public void putShaderData(String id, ByteBuffer byteBuffer)
    {
        shaderDatas.put(id, byteBuffer);
    }
    
    /**
     * Remove the data that is stored under the given {@link Shader} ID
     * 
     * @param id The {@link Shader} ID
     */
    void removeShaderData(String id)
    {
        shaderDatas.remove(id);
    }

    /**
     * Returns the byte buffer containing the data of the {@link Shader}
     * with the given ID, or <code>null</code> if no such data is found.
     * 
     * @param id The {@link Shader} ID
     * @return The byte buffer
     */
    public ByteBuffer getShaderData(String id)
    {
        return shaderDatas.get(id);
    }
    
    /**
     * Returns an unmodifiable view on the map that maps {@link Shader} IDs
     * to the byte buffers storing the raw data.
     * 
     * @return The map
     */
    public Map<String, ByteBuffer> getShaderDatas()
    {
        return Collections.unmodifiableMap(shaderDatas);
    }
    
    /**
     * Convenience method that obtains the raw data of the {@link Shader} with
     * the given ID and returns it as a string. Returns <code>null</code> if 
     * no such data is found.
     * 
     * @param id The {@link Shader} ID
     * @return The shader as a string
     */
    public String getShaderAsString(String id)
    {
        ByteBuffer shaderData = getShaderData(id);
        if (shaderData == null)
        {
            return null;
        }
        byte data[] = new byte[shaderData.capacity()];
        shaderData.slice().get(data);
        return new String(data);
    }
    
    /**
     * Store the given byte buffer under the given {@link Image} ID
     * 
     * @param id The {@link Image} ID
     * @param byteBuffer The byte buffer containing the image data
     */
    public void putImageData(String id, ByteBuffer byteBuffer)
    {
        imageDatas.put(id, byteBuffer);
    }
    
    /**
     * Remove data that is stored under the given {@link Image} ID
     * 
     * @param id The {@link Image} ID
     */
    void removeImageData(String id)
    {
        imageDatas.remove(id);
    }
    
    /**
     * Returns the byte buffer containing the data of the {@link Image}
     * with the given ID, or <code>null</code> if no such data is found.
     * 
     * @param id The {@link Image} ID
     * @return The byte buffer
     */
    public ByteBuffer getImageData(String id)
    {
        return imageDatas.get(id);
    }
    
    /**
     * Returns an unmodifiable view on the map that maps {@link Image} IDs
     * to the byte buffers storing the raw data.
     * 
     * @return The map
     */
    public Map<String, ByteBuffer> getImageDatas()
    {
        return Collections.unmodifiableMap(imageDatas);
    }
    
    /**
     * Convenience method that obtains the raw data of the {@link Image} with
     * the given ID, reads a <code>BufferedImage</code> from this data, and
     * returns it. Returns <code>null</code> if no such data is found, or
     * the data can not be converted into a buffered image.
     * 
     * @param id The {@link Image} ID
     * @return The buffered image
     */
    public BufferedImage getImageAsBufferedImage(String id)
    {
        ByteBuffer imageData = getImageData(id);
        if (imageData == null)
        {
            return null;
        }
        try (InputStream inputStream = 
            Buffers.createByteBufferInputStream(imageData.slice()))
        {
            return ImageIO.read(inputStream);
        }
        catch (IOException e)
        {
            return null;
        }
    }
    
    /**
     * Store the given byte buffer under the given {@link Buffer} ID
     * 
     * @param id The {@link Buffer} ID
     * @param byteBuffer The byte buffer
     */
    public void putBufferData(String id, ByteBuffer byteBuffer)
    {
        bufferDatas.put(id, byteBuffer);
    }
    
    /**
     * Remove the data that is stored under the given {@link Buffer} ID
     * 
     * @param id The {@link Buffer} ID
     */
    void removeBufferData(String id)
    {
        bufferDatas.remove(id);
    }
    
    /**
     * Returns the byte buffer that contains the data of the {@link Buffer}
     * with the given ID, or <code>null</code> if no such data is found.
     * 
     * @param id The {@link Buffer} ID
     * @return The byte buffer
     */
    public ByteBuffer getBufferData(String id)
    {
        return bufferDatas.get(id);
    }
    
    /**
     * Returns an unmodifiable view on the map that maps {@link Buffer} IDs
     * to the byte buffers storing the raw data.
     * 
     * @return The map
     */
    public Map<String, ByteBuffer> getBufferDatas()
    {
        return Collections.unmodifiableMap(bufferDatas);
    }

    /**
     * Store the given byte buffer under the given {@link BufferView} ID
     * 
     * @param id The {@link BufferView} ID
     * @param byteBuffer The byte buffer
     */
    public void putBufferViewData(String id, ByteBuffer byteBuffer)
    {
        bufferViewDatas.put(id, byteBuffer);
    }
    
    /**
     * Remove the data that is stored under the given {@link BufferView} ID
     * 
     * @param id The {@link BufferView} ID
     */
    void removeBufferViewData(String id)
    {
        bufferViewDatas.remove(id);
    }
    
    /**
     * Returns the byte buffer that contains the data of the {@link BufferView} 
     * with the given ID, or <code>null</code> if no such data is found.
     * 
     * @param id The {@link BufferView} ID
     * @return The byte buffer
     */
    public ByteBuffer getBufferViewData(String id)
    {
        return bufferViewDatas.get(id);
    }
    
    /**
     * Returns an unmodifiable view on the map that maps {@link BufferView} IDs
     * to the byte buffers storing the raw data.
     * 
     * @return The map
     */
    public Map<String, ByteBuffer> getBufferViewDatas()
    {
        return Collections.unmodifiableMap(bufferViewDatas);
    }
    
}