/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.hateoas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class Links implements Iterable<Link> {

	private final Collection<Link> links;

	public Links() {
		this.links = new ArrayList<>();
	}

	public Links(Collection<Link> links) {
		this.links = links;
	}

	public Collection<Link> all() {
		return Collections.unmodifiableCollection(links);
	}

	public int size() {
		return links.size();
	}

	public Optional<Link> self() {
		return find(Link.REL_SELF);
	}

	public Optional<Link> get(String rel) {
		return find(rel);
	}

	private Optional<Link> find(String rel) {
		return links.stream()
			.filter(link -> link.is(rel))
				.findFirst();
	}

	public Links add(Link link) {
		links.add(link);
		return this;
	}

	@Override
	public Iterator<Link> iterator() {
		return links.iterator();
	}

	@Override
	public String toString() {
		return links.toString();
	}

	public Collection<Link> unwrap() {
		return Collections.unmodifiableCollection(links);
	}
}
