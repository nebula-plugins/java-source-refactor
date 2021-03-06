/*
 * Copyright 2020 the original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.search;

import lombok.RequiredArgsConstructor;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaSourceVisitor;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.NameTree;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@RequiredArgsConstructor
public class FindType extends JavaSourceVisitor<Set<NameTree>> {
    private final String clazz;

    @Override
    public Set<NameTree> defaultTo(Tree t) {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    @Override
    public Set<NameTree> reduce(Set<NameTree> r1, Set<NameTree> r2) {
        r1.addAll(r2);
        return r1;
    }

    @Override
    public Set<NameTree> visitTypeName(NameTree name) {
        JavaType.Class asClass = TypeUtils.asClass(name.getType());
        if(asClass != null && asClass.getFullyQualifiedName().equals(clazz)) {
            Set<NameTree> names = defaultTo(name);
            names.add(name);
            return names;
        }

        return super.visitTypeName(name);
    }
}
